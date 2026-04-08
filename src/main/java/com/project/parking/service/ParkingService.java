package com.project.parking.service;

import com.project.parking.dto.LicensePlateRecognitionDTO;
import com.project.parking.dto.ParkingSessionDTO;
import com.project.parking.dto.request.ParkingSessionRequest;
import com.project.parking.enums.MemberStatus;
import com.project.parking.enums.PaymentStatus;
import com.project.parking.enums.Role;
import com.project.parking.exceptions.DataNotFoundException;
import com.project.parking.exceptions.InvalidOperationException;
import com.project.parking.model.*;
import com.project.parking.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingService {

    private final ParkingSessionRepository parkingSessionRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final VehicleRepository vehicleRepository;
    private final LicensePlateRecognitionService licensePlateRecognitionService;
    private final CloudinaryService cloudinaryService;
    private final UserService userService;
    private final MemberRepository memberRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final PaymentService paymentService;

    /**
     * Lấy tất cả các phiên gửi xe
     */
    public List<ParkingSessionDTO> getAllParkingSessions() {
        return parkingSessionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy phiên gửi xe theo ID
     */
    public ParkingSessionDTO getParkingSessionById(Long id) throws DataNotFoundException {
        ParkingSession session = parkingSessionRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Phiên gửi xe không tồn tại với ID: " + id));
        return convertToDTO(session);
    }

    public List<ParkingSessionDTO> getParkingSessionByParkingLot(Long id) throws DataNotFoundException {
        List<ParkingSession> sessions = parkingSessionRepository.findByLotId(id);
        if (sessions.isEmpty()) {
            throw new DataNotFoundException("Không tìm thấy phiên gửi xe với ID: " + id);
        }
        return sessions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy các phiên gửi xe đang hoạt động
     */
    public List<ParkingSessionDTO> getActiveParkingSessions() {
        return parkingSessionRepository.findByStatus("ACTIVE").stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy phiên gửi xe đang hoạt động theo ID xe
     */
    public ParkingSessionDTO getActiveSessionByVehicleId(Long vehicleId) throws DataNotFoundException {
        ParkingSession session = parkingSessionRepository.findActiveSessionByVehicleId(vehicleId)
                .orElseThrow(() -> new DataNotFoundException(
                        "Không tìm thấy phiên gửi xe đang hoạt động cho xe với ID: " + vehicleId));
        return convertToDTO(session);
    }

    /**
     * Tìm các phiên gửi xe theo biển số
     */
    public List<ParkingSessionDTO> findSessionsByLicensePlate(String licensePlate) {
        return parkingSessionRepository.findByLicensePlate(licensePlate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Tạo phiên gửi xe mới với thông tin từ ParkingSessionRequest
     */
    @Transactional
    public ParkingSessionDTO createEntrySession(ParkingSessionRequest parkingSessionRequest, MultipartFile image)
            throws DataNotFoundException, IOException, InvalidOperationException {
        // Lấy thông tin bãi đỗ xe
        ParkingLot parkingLot = parkingLotRepository.findById(parkingSessionRequest.getLotId())
                .orElseThrow(() -> new DataNotFoundException(
                        "Bãi đỗ xe không tồn tại với ID: " + parkingSessionRequest.getLotId()));
        if (parkingLot.getAvailableSlots() <= 0) {
            throw new InvalidOperationException("Bãi đỗ xe đã hết chỗ");
        }
        // Tìm hoặc tạo phương tiện
        Vehicle vehicle;
        Optional<Vehicle> optionalVehicle = vehicleRepository
                .findByLicensePlate(parkingSessionRequest.getLicensePlate());
        if (optionalVehicle.isPresent()) {
            vehicle = optionalVehicle.get();
        } else {
            vehicle = new Vehicle();
            vehicle.setLicensePlate(parkingSessionRequest.getLicensePlate());
            vehicle.setVehicleType(parkingSessionRequest.getVehicleType());
            vehicle = vehicleRepository.save(vehicle);
        }
        // Kiểm tra xem xe đã có phiên gửi xe đang hoạt động chưa
        boolean hasActiveSession = parkingSessionRepository.findActiveSessionByVehicleId(vehicle.getId())
                .isPresent();
        if (hasActiveSession) {
            throw new InvalidOperationException("Xe đã có phiên gửi xe đang hoạt động");
        }
        // Tạo phiên gửi xe mới
        ParkingSession session = new ParkingSession();
        session.setLot(parkingLot);
        session.setVehicleId(vehicle.getId());
        session.setEntryTime(LocalDateTime.now());
        session.setLicensePlateImageEntry(cloudinaryService.storeFile(image));
        session.setLicensePlate(vehicle.getLicensePlate());
        session.setStatus("ACTIVE");
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setCode(parkingSessionRequest.getCode());
        ParkingSession savedSession = parkingSessionRepository.save(session);
        // Cập nhật số lượng chỗ trống
        parkingLot.setAvailableSlots(parkingLot.getAvailableSlots() - 1);
        parkingLotRepository.save(parkingLot);
        return convertToDTO(savedSession);
    }



    /**
     * Hoàn thành phiên gửi xe với ID và biển số
     */
    @Transactional
    public ParkingSessionDTO completeExitSession(Long sessionId, String licensePlate) throws DataNotFoundException {
        // Lấy phiên gửi xe hiện tại
        ParkingSession session = parkingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new DataNotFoundException("Phiên gửi xe không tồn tại với ID: " + sessionId));

        if (!session.getStatus().equals("ACTIVE")) {
            throw new IllegalStateException("Không thể hoàn thành phiên gửi xe không hoạt động");
        }

        // Cập nhật thời gian ra
        LocalDateTime exitTime = LocalDateTime.now();
        session.setExitTime(exitTime);

        // Cập nhật hình ảnh biển số lúc ra
        session.setLicensePlateImageExit(licensePlate);

        // Lấy thông tin xe
        Vehicle vehicle = vehicleRepository.findById(session.getVehicleId()).orElse(null);

        BigDecimal totalCost;

//        // Kiểm tra xem xe có thuộc về member có thẻ đang hoạt động không
//        if (vehicle != null && isVehicleOwnedByActiveMember(vehicle)) {
//            // Member không phải trả phí theo giờ/ngày (đã đóng phí tháng/quý/năm)
//            totalCost = BigDecimal.ZERO;
//            log.info("Vehicle {} belongs to active member - no parking fee charged", vehicle.getLicensePlate());
//        } else {
            // Tính phí bình thường cho khách vãng lai
            BigDecimal hourlyRate = session.getLot().getHourlyRate() != null ? session.getLot().getHourlyRate()
                    : BigDecimal.valueOf(10000); // Giá mặc định nếu không có

            Duration duration = Duration.between(session.getEntryTime(), exitTime);
            long hours = duration.toHours() + (duration.toMinutes() % 60 > 0 ? 1 : 0); // Làm tròn lên giờ

            if (hours < 24) {
                totalCost = hourlyRate.multiply(BigDecimal.valueOf(Math.max(hours, 1))); // Tối thiểu 1 giờ
            } else {
                long days = hours / 24;
                long remainingHours = hours % 24;
                BigDecimal dailyRate = session.getLot().getDailyRate() != null ? session.getLot().getDailyRate()
                        : hourlyRate.multiply(BigDecimal.valueOf(20)); // Mặc định giá ngày = 20 giờ

                totalCost = dailyRate.multiply(BigDecimal.valueOf(days))
                        .add(hourlyRate.multiply(BigDecimal.valueOf(remainingHours)));
            }
//        }

        session.setTotalCost(totalCost);
        session.setStatus("COMPLETED");
        session.setUpdatedAt(LocalDateTime.now());

        // Cập nhật số lượng chỗ trống
        ParkingLot parkingLot = session.getLot();
        parkingLot.setAvailableSlots(parkingLot.getAvailableSlots() + 1);
        parkingLotRepository.save(parkingLot);

        ParkingSession completedSession = parkingSessionRepository.save(session);
        return convertToDTO(completedSession);
    }

    /**
     * Hoàn thành phiên gửi xe với mã code và nhận diện biển số tự động
     */
    @Transactional
    public ParkingSessionDTO completeExitSessionWithRecognition(Integer code, MultipartFile licensePlateImage,
            String licensePlate, String paymentMethod)
            throws IOException, DataNotFoundException, InvalidOperationException {
        // Tìm phiên gửi xe
        ParkingSession session = parkingSessionRepository.findByCode(code)
                .orElseThrow(() -> new DataNotFoundException("Phiên gửi xe không tồn tại với code: " + code));
        if (!session.getStatus().equals("ACTIVE")) {
            throw new InvalidOperationException("Phiên gửi xe đã kết thúc");
        }
        // Kiểm tra biển số xe
        Vehicle vehicle = vehicleRepository.findById(session.getVehicleId())
                .orElseThrow(() -> new DataNotFoundException("Xe không tồn tại"));
        if (!vehicle.getLicensePlate().equalsIgnoreCase(licensePlate)) {
            throw new InvalidOperationException("Biển số xe không khớp với phiên gửi xe");
        }
        Optional<PaymentHistory> paymentHistory = paymentHistoryRepository.findBySessionId(session.getId());
        if(paymentHistory.isEmpty() && session.getTotalCost().compareTo(BigDecimal.ZERO)!=0){

                throw new DataNotFoundException("Lịch sử thanh toán không tồn tại với code: " + code);
        }
        if(paymentHistory.isPresent()){
            PaymentHistory payment = paymentHistory.get();
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setPaymentTime(LocalDateTime.now());
            paymentHistoryRepository.save(payment);
        }


        ParkingLot parkingLot = session.getLot();
        LocalDateTime exitTime = LocalDateTime.now();
        // Cập nhật phiên gửi xe
        session.setExitTime(exitTime);
        session.setLicensePlateImageExit(cloudinaryService.storeFile(licensePlateImage));
        session.setStatus("COMPLETED");
        session.setUpdatedAt(LocalDateTime.now());
        ParkingSession savedSession = parkingSessionRepository.save(session);

        // Cập nhật số lượng chỗ trống
        parkingLot.setAvailableSlots(parkingLot.getAvailableSlots() + 1);
        parkingLotRepository.save(parkingLot);
        return convertToDTO(savedSession);
    }

    public BigDecimal caculateTotalCost(ParkingSession session) throws DataNotFoundException, InvalidOperationException {
        // Tìm phiên gửi xe

        LocalDateTime exitTime = LocalDateTime.now();
        BigDecimal totalCost;
        long minutes = ChronoUnit.MINUTES.between(session.getEntryTime(), exitTime);
        long hours = (minutes + 59) / 60; // Làm tròn lên giờ
        ParkingLot parkingLot = session.getLot();

        if (hours < 24) {
            totalCost = parkingLot.getHourlyRate().multiply(BigDecimal.valueOf(hours));
        } else {
            long days = hours / 24;
            long remainingHours = hours % 24;
            totalCost = parkingLot.getDailyRate().multiply(BigDecimal.valueOf(days))
                    .add(parkingLot.getHourlyRate().multiply(BigDecimal.valueOf(remainingHours)));
        }
        session.setTotalCost(totalCost);
        parkingSessionRepository.save(session);
        return totalCost;
    }

    public ResponseEntity<?> processPayment(Integer code, String paymentMethod) throws DataNotFoundException, InvalidOperationException {
        ParkingSession session = parkingSessionRepository.findByCode(code)
                .orElseThrow(() -> new DataNotFoundException("Phiên gửi xe không tồn tại với code: " + code));
        if (!session.getStatus().equals("ACTIVE")) {
            throw new InvalidOperationException("Phiên gửi xe đã kết thúc");
        }
        if(session.getEntryTime().plusMinutes(10).isAfter(LocalDateTime.now())) {
            session.setTotalCost(BigDecimal.ZERO);
            parkingSessionRepository.save(session);
            return ResponseEntity.ok(0);
        }
        BigDecimal totalCost= caculateTotalCost(session);
        if (paymentMethod.equalsIgnoreCase("CASH")) {
            PaymentHistory paymentHistory = PaymentHistory.builder()
                    .paymentStatus(PaymentStatus.PENDING)
                    .paymentMethod("CASH")
                    .sessionId(session.getId())
                    .amount(totalCost)
                    .build();
            paymentHistoryRepository.save(paymentHistory);
                        return ResponseEntity.ok(Map.of(
                                        "paymentMethod", "CASH",
                                        "totalCost", totalCost));
        } else if (paymentMethod.equalsIgnoreCase("MOMO")) {
            PaymentHistory paymentHistory = PaymentHistory.builder()
                    .paymentStatus(PaymentStatus.PENDING)
                    .paymentMethod("MOMO")
                    .sessionId(session.getId())
                    .amount(totalCost)
                    .build();
            paymentHistoryRepository.save(paymentHistory);
            String paymentUrl= paymentService.createParkingPaymentUrl(totalCost);
                        return ResponseEntity.ok(Map.of(
                                        "paymentMethod", "MOMO",
                                        "paymentUrl", paymentUrl));
        } else {
                        return ResponseEntity.badRequest().body(Map.of(
                                        "error", "Phương thức thanh toán không hợp lệ: " + paymentMethod
                                                        + ". Chỉ hỗ trợ CASH hoặc MOMO."));
                }
    }

//    public BigDecimal processingPaymentCash(ParkingSession session) throws DataNotFoundException, InvalidOperationException {
//
//
//        PaymentHistory paymentHistory = PaymentHistory.builder()
//                .paymentStatus(PaymentStatus.PENDING)
//                .paymentMethod("CASH")
//                .sessionId(session.getId())
//                .amount(totalCost)
//                .build();
//        paymentHistoryRepository.save(paymentHistory);
//        return totalCost;
//    }

//    public String processingPaymentMomo(ParkingSession session) throws InvalidOperationException, DataNotFoundException {
//        BigDecimal totalCost= caculateTotalCost(session);
//        PaymentHistory paymentHistory = PaymentHistory.builder()
//                .paymentStatus(PaymentStatus.PENDING)
//                .paymentMethod("MOMO")
//                .sessionId(session.getId())
//                .amount(totalCost)
//                .build();
//        paymentHistoryRepository.save(paymentHistory);
//        return paymentService.createParkingPaymentUrl(totalCost);
//
//    }

    /**
     * Tạo phiên gửi xe mới dành riêng cho member (kiểm tra thẻ thành viên trước)
     */
    @Transactional
    public ParkingSessionDTO createMemberEntrySession(ParkingSessionRequest parkingSessionRequest, MultipartFile image)
            throws DataNotFoundException, IOException, InvalidOperationException {
        // Lấy thông tin bãi đỗ xe
        ParkingLot parkingLot = parkingLotRepository.findById(parkingSessionRequest.getLotId())
                .orElseThrow(() -> new DataNotFoundException(
                        "Bãi đỗ xe không tồn tại với ID: " + parkingSessionRequest.getLotId()));
        if (parkingLot.getAvailableSlots() <= 0) {
            throw new InvalidOperationException("Bãi đỗ xe đã hết chỗ");
        }

        // Tìm member theo member code
        Member member = memberRepository.findByMemberCode(parkingSessionRequest.getMemberCode())
                .orElseThrow(() -> new DataNotFoundException(
                "Khong ton tai member voi member code: " + parkingSessionRequest.getMemberCode()));
        // Tìm phương tiện theo biển số
        Vehicle vehicle = vehicleRepository.findByLicensePlate(parkingSessionRequest.getLicensePlate())
                .orElseThrow(() -> new DataNotFoundException(
                        "Không tìm thấy xe với biển số: " + parkingSessionRequest.getLicensePlate()));

        // Kiem tra xe co thuoc member khong
        if(!member.getVehicles().contains(vehicle)){
            throw new InvalidOperationException(
                    "Xe không thuộc về thành viên voi member code: " + parkingSessionRequest.getMemberCode());
        }
        // Kiểm tra xe có thuộc về member đang hoạt động không
        if (!isVehicleOwnedByActiveMember(member)) {
            throw new InvalidOperationException(
                    "Xe không thuộc về thành viên có thẻ đang hoạt động. Vui lòng sử dụng API check-in thông thường.");
        }
        // Kiểm tra xem xe đã có phiên gửi xe đang hoạt động chưa
        boolean hasActiveSession = parkingSessionRepository.findActiveSessionByVehicleId(vehicle.getId()).isPresent();
        if (hasActiveSession) {
            throw new InvalidOperationException("Xe đã có phiên gửi xe đang hoạt động");
        }
        // Tạo phiên gửi xe mới với phí = 0 (đã đóng phí thành viên)
        ParkingSession session = new ParkingSession();
        session.setLicensePlate(vehicle.getLicensePlate());
        session.setLot(parkingLot);
        session.setVehicleId(vehicle.getId());
        session.setEntryTime(LocalDateTime.now());
        session.setLicensePlateImageEntry(cloudinaryService.storeFile(image));
        session.setStatus("ACTIVE");
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setCode(null);
        session.setMemberId(member.getId());
        session.setTotalCost(BigDecimal.ZERO); // Member không thu phí theo lượt
        ParkingSession savedSession = parkingSessionRepository.save(session);
        // Cập nhật số lượng chỗ trống
        parkingLot.setAvailableSlots(parkingLot.getAvailableSlots() - 1);
        parkingLotRepository.save(parkingLot);
        log.info("Member check-in: vehicle {} entered lot {}", vehicle.getLicensePlate(), parkingLot.getId());
        return convertToDTO(savedSession);
    }

    /**
     * Hoàn thành phiên gửi xe dành riêng cho member (phí = 0, xác thực thẻ thành
     * viên)
     */
    @Transactional
    public ParkingSessionDTO completeMemberExitSession(ParkingSessionRequest parkingSessionRequest, MultipartFile licensePlateImage)
            throws IOException, DataNotFoundException, InvalidOperationException {
        // Tìm phiên gửi xe theo code

        // Tìm member theo member code
        Member member = memberRepository.findByMemberCode(parkingSessionRequest.getMemberCode())
                .orElseThrow(() -> new DataNotFoundException(
                        "Khong ton tai member voi member code: " + parkingSessionRequest.getMemberCode()));
        // Tìm phương tiện theo biển số
        Vehicle vehicle = vehicleRepository.findByLicensePlate(parkingSessionRequest.getLicensePlate())
                .orElseThrow(() -> new DataNotFoundException(
                        "Không tìm thấy xe với biển số: " + parkingSessionRequest.getLicensePlate()));

        // Kiem tra xe co thuoc member khong
        if(!member.getVehicles().contains(vehicle)){
            throw new InvalidOperationException(
                    "Xe không thuộc về thành viên voi member code: " + parkingSessionRequest.getMemberCode());
        }
        // Xác minh xe vẫn thuộc về member đang hoạt động
        if (!isVehicleOwnedByActiveMember(member)) {
            throw new InvalidOperationException(
                    "Xe không thuộc về thành viên có thẻ đang hoạt động. Vui lòng sử dụng API check-out thông thường.");
        }
        ParkingSession session = parkingSessionRepository.findByMemberIdAndVehicle(member.getId(),vehicle.getId())
                .orElseThrow(() -> new DataNotFoundException("Phiên gửi xe không tồn tại với member code: " + parkingSessionRequest.getMemberCode() + " và biển số : " + parkingSessionRequest.getLicensePlate() + "vehicleId: "+ vehicle.getId()));
        if (!session.getStatus().equals("ACTIVE")) {
            throw new InvalidOperationException("Phiên gửi xe đã kết thúc");
        }
        // Phí = 0 vì là member
        LocalDateTime exitTime = LocalDateTime.now();
        session.setExitTime(exitTime);
        session.setLicensePlateImageExit(cloudinaryService.storeFile(licensePlateImage));
        session.setStatus("COMPLETED");
        session.setTotalCost(BigDecimal.ZERO);
        session.setUpdatedAt(LocalDateTime.now());
        ParkingSession savedSession = parkingSessionRepository.save(session);
        // Cập nhật số lượng chỗ trống
        ParkingLot parkingLot = session.getLot();
        parkingLot.setAvailableSlots(parkingLot.getAvailableSlots() + 1);
        parkingLotRepository.save(parkingLot);
        log.info("Member check-out: vehicle {} exited lot {}, totalCost=0", vehicle.getLicensePlate(),
                parkingLot.getId());
        return convertToDTO(savedSession);
    }

    /**
     * Kiểm tra xem xe có thuộc về member có thẻ đang hoạt động không
     */
    private boolean isVehicleOwnedByActiveMember(Member member) {
        if(member==null){
            return false;
        }

        // Kiểm tra member status là ACTIVE
        if (member.getMemberStatus() != MemberStatus.ACTIVE) {
            return false;
        }

        // Kiểm tra thẻ còn hạn
        if (member.getMembershipExpiryDate() == null) {
            return false;
        }

        return member.getMembershipExpiryDate().isAfter(LocalDateTime.now());
    }

    public ParkingSessionDTO getParkingSessionByCode(Integer code)
            throws DataNotFoundException, InvalidOperationException {
        ParkingSession session = parkingSessionRepository.findByCode(code)
                .orElseThrow(() -> new DataNotFoundException("Phiên gửi xe không tồn tại với code: " + code));
        ParkingSessionDTO sessionDTO = convertToDTO(session);
        return sessionDTO;

    }

    public ParkingSessionDTO getParkingSessionByUser(Long userId)
            throws DataNotFoundException {
        User user = userService.findById(userId);
        ParkingSession session = parkingSessionRepository.findByUserId(userId)
                .orElseThrow(() -> new DataNotFoundException("Phiên gửi xe không tồn tại với user: " + userId));
        ParkingSessionDTO sessionDTO = convertToDTO(session);
        return sessionDTO;
    }

    public List<ParkingSessionDTO> getParkingSessionByDateTime(LocalDateTime dateStart, LocalDateTime dateEnd)
            throws DataNotFoundException {
        List<ParkingSession> sessions = parkingSessionRepository.findByDateTime(dateStart, dateEnd);
        return sessions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi ParkingSession thành ParkingSessionDTO
     */
    private ParkingSessionDTO convertToDTO(ParkingSession session) {
        Vehicle vehicle = vehicleRepository.findById(session.getVehicleId()).orElse(null);
        String licensePlate = vehicle != null ? vehicle.getLicensePlate() : "Unknown";
        boolean isMemberVehicle = vehicle != null && isVehicleOwnedByActiveMember(vehicle.getMember());

        // Lấy memberCode từ Member model nếu xe thuộc member
        String memberCode = null;
        if (isMemberVehicle && vehicle.getMember() != null) {
            memberCode = vehicle.getMember().getMemberCode();
        }

        return ParkingSessionDTO.builder()
                .id(session.getId())
                .lotId(session.getLot().getId())
                .vehicleId(session.getVehicleId())
                .licensePlate(licensePlate)
                .entryTime(session.getEntryTime())
                .exitTime(session.getExitTime())
                .licensePlateImageEntry(session.getLicensePlateImageEntry())
                .licensePlateImageExit(session.getLicensePlateImageExit())
                .status(session.getStatus())
                .totalCost(session.getTotalCost())
                .code(session.getCode())
                .isMemberVehicle(isMemberVehicle)
                .memberCode(memberCode)
                .build();
    }
}
