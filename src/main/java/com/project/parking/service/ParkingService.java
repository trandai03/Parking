package com.project.parking.service;

import com.project.parking.dto.LicensePlateRecognitionDTO;
import com.project.parking.dto.ParkingSessionDTO;
import com.project.parking.dto.request.ParkingSessionRequest;
import com.project.parking.enums.MemberStatus;
import com.project.parking.enums.Role;
import com.project.parking.exceptions.DataNotFoundException;
import com.project.parking.exceptions.InvalidOperationException;
import com.project.parking.model.ParkingLot;
import com.project.parking.model.ParkingSession;
import com.project.parking.model.User;
import com.project.parking.model.Vehicle;
import com.project.parking.repository.MemberRepository;
import com.project.parking.repository.ParkingLotRepository;
import com.project.parking.repository.ParkingSessionRepository;
import com.project.parking.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
     * Tạo phiên gửi xe mới với nhận diện biển số tự động
     */
    // @Transactional
    // public ParkingSessionDTO createEntrySessionWithRecognition(Long lotId,
    // MultipartFile licensePlateImage)
    // throws IOException, DataNotFoundException, InvalidOperationException {
    // // Nhận diện biển số xe
    // String licensePlate =
    // licensePlateRecognitionService.recognizeLicensePlate(licensePlateImage);
    // if (licensePlate == null) {
    // throw new InvalidOperationException("Không thể nhận diện biển số xe");
    // }
    //
    // // Kiểm tra bãi đỗ xe
    // ParkingLot parkingLot = parkingLotRepository.findById(lotId)
    // .orElseThrow(() -> new DataNotFoundException("Bãi đỗ xe không tồn tại với ID:
    // " + lotId));
    //
    // if (parkingLot.getAvailableSlots() <= 0) {
    // throw new InvalidOperationException("Bãi đỗ xe đã hết chỗ");
    // }
    //
    // // Tìm hoặc tạo xe mới
    // Vehicle vehicle = vehicleRepository.findByLicensePlate(licensePlate)
    // .orElseGet(() -> {
    // Vehicle newVehicle = new Vehicle();
    // newVehicle.setLicensePlate(licensePlate);
    // newVehicle.setVehicleType("UNKNOWN"); // Thiết lập loại xe mặc định
    // return vehicleRepository.save(newVehicle);
    // });
    //
    // // Kiểm tra xem xe đã có phiên gửi xe đang hoạt động chưa
    // boolean hasActiveSession =
    // parkingSessionRepository.findActiveSessionByVehicleId(vehicle.getId())
    // .isPresent();
    // if (hasActiveSession) {
    // throw new InvalidOperationException("Xe đã có phiên gửi xe đang hoạt động");
    // }
    //
    // // Tạo phiên gửi xe mới
    // ParkingSession session = new ParkingSession();
    // session.setLot(parkingLot);
    // session.setVehicleId(vehicle.getId());
    // session.setEntryTime(LocalDateTime.now());
    // session.setLicensePlateImageEntry(cloudinaryService.storeFile(licensePlateImage));
    // session.setStatus("ACTIVE");
    // session.setCreatedAt(LocalDateTime.now());
    // session.setUpdatedAt(LocalDateTime.now());
    //
    // ParkingSession savedSession = parkingSessionRepository.save(session);
    //
    // // Cập nhật số lượng chỗ trống
    // parkingLot.setAvailableSlots(parkingLot.getAvailableSlots() - 1);
    // parkingLotRepository.save(parkingLot);
    //
    // return convertToDTO(savedSession);
    // }

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

        // Kiểm tra xem xe có thuộc về member có thẻ đang hoạt động không
        if (vehicle != null && isVehicleOwnedByActiveMember(vehicle)) {
            // Member không phải trả phí theo giờ/ngày (đã đóng phí tháng/quý/năm)
            totalCost = BigDecimal.ZERO;
            log.info("Vehicle {} belongs to active member - no parking fee charged", vehicle.getLicensePlate());
        } else {
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
        }

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
            String licensePlate)
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

        // Tính phí gửi xe
        LocalDateTime exitTime = LocalDateTime.now();
        BigDecimal totalCost;

        // Kiểm tra xem xe có thuộc về member có thẻ đang hoạt động không
        if (isVehicleOwnedByActiveMember(vehicle)) {
            // Member không phải trả phí theo giờ/ngày (đã đóng phí tháng/quý/năm)
            totalCost = BigDecimal.ZERO;
            log.info("Vehicle {} belongs to active member - no parking fee charged", vehicle.getLicensePlate());
        } else {
            // Tính phí bình thường cho khách vãng lai
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
        }

        // Cập nhật phiên gửi xe
        session.setExitTime(exitTime);
        session.setLicensePlateImageExit(cloudinaryService.storeFile(licensePlateImage));
        session.setStatus("COMPLETED");
        session.setTotalCost(totalCost);
        session.setUpdatedAt(LocalDateTime.now());
        ParkingSession savedSession = parkingSessionRepository.save(session);

        // Cập nhật số lượng chỗ trống
        ParkingLot parkingLot = session.getLot();
        parkingLot.setAvailableSlots(parkingLot.getAvailableSlots() + 1);
        parkingLotRepository.save(parkingLot);
        return convertToDTO(savedSession);
    }

    /**
     * Kiểm tra xem xe có thuộc về member có thẻ đang hoạt động không
     */
    private boolean isVehicleOwnedByActiveMember(Vehicle vehicle) {
        if (vehicle.getUser() == null) {
            return false;
        }

        User owner = vehicle.getUser();

        // Tìm Member record thông qua userId
        var memberOpt = memberRepository.findByUserId(owner.getId());

        if (memberOpt.isEmpty()) {
            return false; // User không có membership
        }

        var member = memberOpt.get();

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
        boolean isMemberVehicle = vehicle != null && isVehicleOwnedByActiveMember(vehicle);

        // Lấy memberCode từ Member model nếu xe thuộc member
        String memberCode = null;
        if (isMemberVehicle && vehicle.getUser() != null) {
            var memberOpt = memberRepository.findByUserId(vehicle.getUser().getId());
            if (memberOpt.isPresent()) {
                memberCode = memberOpt.get().getMemberCode();
            }
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
