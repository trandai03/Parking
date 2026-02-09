package com.project.parking.service;

import com.project.parking.dto.request.*;
import com.project.parking.enums.MemberStatus;
import com.project.parking.enums.Role;
import com.project.parking.exceptions.DataNotFoundException;
import com.project.parking.exceptions.InvalidOperationException;
import com.project.parking.model.*;
import com.project.parking.repository.*;
import com.project.parking.response.member.MemberResponse;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingPlanRepository parkingPlanRepository;
    private final InvoiceService invoiceService;
    private final EmailService emailService;

    /**
     * Get all members
     */
    @Transactional(readOnly = true)
    public List<MemberResponse> getAllMembers() {
        log.info("Fetching all members");
        List<Member> members = memberRepository.findAll();
        return MemberResponse.fromMembers(members);
    }

    /**
     * Get members by parking lot
     */
    @Transactional(readOnly = true)
    public List<MemberResponse> getMembersByParkingLot(Long parkingLotId) {
        log.info("Fetching members for parking lot: {}", parkingLotId);
        List<Member> members = memberRepository.findByParkingLotId(parkingLotId);
        return MemberResponse.fromMembers(members);
    }

    /**
     * Get member by ID
     */
    @Transactional(readOnly = true)
    public MemberResponse getMemberById(Long id) throws DataNotFoundException {
        log.info("Fetching member with id: {}", id);
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Member không tồn tại với ID: " + id));
        return MemberResponse.fromMember(member);
    }

    /**
     * Get member by member code
     */
    @Transactional(readOnly = true)
    public MemberResponse getMemberByCode(String memberCode) throws DataNotFoundException {
        log.info("Fetching member with code: {}", memberCode);
        Member member = memberRepository.findByMemberCode(memberCode)
                .orElseThrow(() -> new DataNotFoundException("Member không tồn tại với mã: " + memberCode));
        return MemberResponse.fromMember(member);
    }

    /**
     * Get member by user ID
     */
    @Transactional(readOnly = true)
    public MemberResponse getMemberByUserId(Long userId) throws DataNotFoundException {
        log.info("Fetching member for user: {}", userId);
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new DataNotFoundException("User chưa đăng ký member"));
        return MemberResponse.fromMember(member);
    }

    /**
     * Check if user has membership
     */
    @Transactional(readOnly = true)
    public boolean hasMembership(Long userId) {
        return memberRepository.existsByUserId(userId);
    }

    /**
     * Register user as member (User must be registered first)
     */
    @Transactional
    public MemberResponse registerMember(Long userId, CreateMemberRequest request) throws Exception {
        log.info("Registering member for user: {}", userId);

        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User không tồn tại với ID: " + userId));

        // Check if user already has membership

        Member member = memberRepository.findByUserId(userId).orElse(null);

        if (member != null && member.getMemberStatus() != MemberStatus.PENDING
                && member.getMemberStatus() != MemberStatus.REJECTED
                && member.getMemberStatus() != MemberStatus.CANCELLED) {
            throw new DataIntegrityViolationException("User đã đăng ký member rồi");
        }

        // Get parking lot if specified
        ParkingLot parkingLot = null;
        if (request.getParkingLotId() != null) {
            parkingLot = parkingLotRepository.findById(request.getParkingLotId())
                    .orElseThrow(() -> new DataNotFoundException(
                            "Bãi đỗ xe không tồn tại với ID: " + request.getParkingLotId()));
        }

        // Get plan if specified
        ParkingPlan plan = parkingPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new DataNotFoundException("Gói không tồn tại với ID: " + request.getPlanId()));
        BigDecimal fee = plan.getPrice();

        String memberCode = generateMemberCode();

        user.setDateOfBirth(request.getDateOfBirth());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        user.setAddress(request.getAddress());
        user.setFullname(request.getFullname());
        userRepository.save(user);
        if (member != null) {
            member.setParkingLot(parkingLot);
            member.setParkingPlan(plan);
            member.setMemberCode(memberCode);
            member.setMemberStatus(MemberStatus.PENDING);
            member.setMembershipStartDate(null);
            member.setMembershipExpiryDate(null);
            member.setMembershipFee(fee);
            member.setRoomNumber(request.getRoomNumber());
            member.setMemberStatus(MemberStatus.PENDING);
        } else {
            member = Member.builder()
                    .user(user)
                    .parkingLot(parkingLot)
                    .parkingPlan(plan)
                    .memberCode(memberCode)
                    .memberStatus(MemberStatus.PENDING)
                    .membershipStartDate(null) // Will be set when approved
                    .membershipExpiryDate(null) // Will be set when approved
                    .membershipFee(fee)
                    .roomNumber(request.getRoomNumber())
                    .build();
        }
        // Note: User role will be updated to MEMBER only after approval

        Member savedMember = memberRepository.save(member);
        log.info("Created new member registration with code: {} (PENDING approval)", memberCode);

        // Create vehicle if license plate provided
        if (request.getLicensePlate() != null && !request.getLicensePlate().isEmpty()) {
            createVehicleForMember(savedMember, request.getLicensePlate(), request.getVehicleType());
        }

        // Send pending notification email
        try {
            sendPendingNotificationEmail(savedMember);
        } catch (MessagingException e) {
            log.error("Failed to send pending notification email to: {}", user.getEmail(), e);
        }

        return MemberResponse.fromMember(savedMember);
    }

    /**
     * Approve member registration (Owner only)
     */
    @Transactional
    public MemberResponse approveMember(Long memberId) throws DataNotFoundException, InvalidOperationException {
        log.info("Approving member with id: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new DataNotFoundException("Member không tồn tại với ID: " + memberId));

        if (member.getMemberStatus() != MemberStatus.PENDING) {
            throw new InvalidOperationException("Chỉ có thể duyệt member đang ở trạng thái chờ duyệt");
        }

        // Set membership dates when approved
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime expiryDate = calculateExpiryDate(startDate, member.getParkingPlan());

        member.setMemberStatus(MemberStatus.WAITING_PAYMENT);
        member.setMembershipStartDate(startDate);
        member.setMembershipExpiryDate(expiryDate);
        member.setMembershipAcceptDate(LocalDateTime.now());

        // Update user role to MEMBER
        User user = member.getUser();
        user.setRole(Role.MEMBER);
        userRepository.save(user);

        Member approvedMember = memberRepository.save(member);
        log.info("Approved member with id: {}", memberId);

        // Tạo Invoice cho member (thay vì PaymentHistory)
        Invoice invoice = invoiceService.createMembershipInvoice(memberId);

        // Send welcome email với thông tin thanh toán
        try {
            sendWelcomeEmailWithPaymentInfo(approvedMember, invoice);
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to member: {}", user.getEmail(), e);
        }

        return MemberResponse.fromMember(approvedMember);
    }

    // Phương thức createPaymentHistoryForMember đã được thay thế bởi
    // invoiceService.createMembershipInvoice()

    /**
     * Gửi email chào mừng với thông tin thanh toán
     */
    private void sendWelcomeEmailWithPaymentInfo(Member member, Invoice invoice) throws MessagingException {
        User user = member.getUser();
        String subject = "Đăng ký thành viên được duyệt - Vui lòng thanh toán";
        String htmlMessage = "<!DOCTYPE html>"
                + "<html>"
                + "<head><meta charset=\"UTF-8\"></head>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;\">"
                + "<h2 style=\"color: #4CAF50;\">Chúc mừng! Đăng ký của bạn đã được duyệt</h2>"
                + "<p>Xin chào <strong>" + user.getFullname() + "</strong>,</p>"
                + "<p>Đăng ký thành viên của bạn đã được duyệt. Vui lòng thanh toán trong vòng <strong>5 ngày</strong> để kích hoạt thẻ.</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1); margin: 20px 0;\">"
                + "<h3 style=\"color: #333;\">Thông tin thanh toán:</h3>"
                + "<p><strong>Mã thẻ:</strong> " + member.getMemberCode() + "</p>"
                + "<p><strong>Mã hóa đơn:</strong> " + invoice.getInvoiceCode() + "</p>"
                + "<p><strong>Gói:</strong> " + member.getParkingPlan().getName() + "</p>"
                + "<p><strong>Số tiền:</strong> " + invoice.getAmount() + " VND</p>"
                + "<p><strong>Hạn thanh toán:</strong> <span style=\"color: red;\">"
                + invoice.getPaymentDeadline().toLocalDate() + "</span></p>"
                + "</div>"
                + "<div style=\"background-color: #fff3e0; padding: 15px; border-radius: 5px; margin: 20px 0;\">"
                + "<p style=\"color: #ff9800; margin: 0;\"><strong>⚠️ Lưu ý:</strong> Nếu không thanh toán trong thời hạn, thẻ của bạn sẽ bị khóa.</p>"
                + "</div>"
                + "<p>Vui lòng đăng nhập vào hệ thống và thực hiện thanh toán.</p>"
                + "<p style=\"margin-top: 20px;\">Trân trọng,<br/>Đội ngũ Parking Management</p>"
                + "</div>"
                + "</body>"
                + "</html>";

        emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        log.info("Sent welcome email with payment info to member: {}", member.getId());
    }

    /**
     * Reject member registration (Owner only)
     */
    @Transactional
    public MemberResponse rejectMember(Long memberId, String reason)
            throws DataNotFoundException, InvalidOperationException {
        log.info("Rejecting member with id: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new DataNotFoundException("Member không tồn tại với ID: " + memberId));

        if (member.getMemberStatus() != MemberStatus.PENDING) {
            throw new InvalidOperationException("Chỉ có thể từ chối member đang ở trạng thái chờ duyệt");
        }

        member.setMemberStatus(MemberStatus.REJECTED);
        member.setLockReason(reason != null ? reason : "Đơn đăng ký bị từ chối");
        member.setLockedAt(LocalDateTime.now());

        Member rejectedMember = memberRepository.save(member);
        log.info("Rejected member with id: {}", memberId);

        // Send rejection email
        try {
            sendRejectionEmail(rejectedMember, reason);
        } catch (MessagingException e) {
            log.error("Failed to send rejection email to member: {}", member.getUser().getEmail(), e);
        }

        return MemberResponse.fromMember(rejectedMember);
    }

    /**
     * Get pending members (for Owner to review)
     */
    @Transactional(readOnly = true)
    public List<MemberResponse> getPendingMembers() {
        log.info("Getting pending members");
        List<Member> members = memberRepository.findByMemberStatus(MemberStatus.PENDING);
        return MemberResponse.fromMembers(members);
    }

    /**
     * Get pending members by parking lot (for Owner to review)
     */
    @Transactional(readOnly = true)
    public List<MemberResponse> getPendingMembersByParkingLot(Long parkingLotId) {
        log.info("Getting pending members for parking lot: {}", parkingLotId);
        List<Member> members = memberRepository.findByParkingLotIdAndMemberStatus(parkingLotId, MemberStatus.PENDING);
        return MemberResponse.fromMembers(members);
    }

    /**
     * Update member information
     */
    @Transactional
    public MemberResponse updateMember(Long id, UpdateMemberRequest request) throws DataNotFoundException {
        log.info("Updating member with id: {}", id);

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Member không tồn tại với ID: " + id));

        User user = member.getUser();

        // Update user fields if provided
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new DataIntegrityViolationException("Username đã tồn tại");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DataIntegrityViolationException("Email đã tồn tại");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFullname() != null) {
            user.setFullname(request.getFullname());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }

        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }

        userRepository.save(user);
        Member updatedMember = memberRepository.save(member);
        log.info("Updated member with id: {}", id);

        return MemberResponse.fromMember(updatedMember);
    }

    /**
     * Lock member account
     */
    @Transactional
    public MemberResponse lockMember(Long id, LockMemberRequest request)
            throws DataNotFoundException, InvalidOperationException {
        log.info("Locking member with id: {}", id);

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Member không tồn tại với ID: " + id));

        if (member.getMemberStatus() == MemberStatus.LOCKED) {
            throw new InvalidOperationException("Member đã bị khóa trước đó");
        }

        member.setMemberStatus(MemberStatus.LOCKED);
        member.setLockedAt(LocalDateTime.now());
        member.setLockReason(request.getLockReason());

        Member lockedMember = memberRepository.save(member);
        log.info("Locked member with id: {}", id);

        return MemberResponse.fromMember(lockedMember);
    }

    /**
     * Unlock member account
     */
    @Transactional
    public MemberResponse unlockMember(Long id) throws DataNotFoundException, InvalidOperationException {
        log.info("Unlocking member with id: {}", id);

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Member không tồn tại với ID: " + id));

        if (member.getMemberStatus() != MemberStatus.LOCKED) {
            throw new InvalidOperationException("Member không ở trạng thái bị khóa");
        }

        // Check if membership is still valid
        if (member.getMembershipExpiryDate() != null &&
                member.getMembershipExpiryDate().isBefore(LocalDateTime.now())) {
            member.setMemberStatus(MemberStatus.EXPIRED);
        } else {
            member.setMemberStatus(MemberStatus.ACTIVE);
        }

        member.setLockedAt(null);
        member.setLockReason(null);

        Member unlockedMember = memberRepository.save(member);
        log.info("Unlocked member with id: {}", id);

        return MemberResponse.fromMember(unlockedMember);
    }

    /**
     * Cancel member card (permanent)
     */
    @Transactional
    public MemberResponse cancelMember(Long id, String reason) throws DataNotFoundException {
        log.info("Cancelling member with id: {}", id);

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Member không tồn tại với ID: " + id));

        member.setMemberStatus(MemberStatus.CANCELLED);
        member.setLockedAt(LocalDateTime.now());
        member.setLockReason(reason != null ? reason : "Thẻ bị hủy theo yêu cầu");

        // Update user role back to CUSTOMER
        User user = member.getUser();
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);

        Member cancelledMember = memberRepository.save(member);
        log.info("Cancelled member with id: {}", id);

        return MemberResponse.fromMember(cancelledMember);
    }

    /**
     * Renew member subscription
     */
    @Transactional
    public MemberResponse renewMember(Long id, RenewMemberRequest request)
            throws DataNotFoundException, InvalidOperationException {
        log.info("Renewing member with id: {}", id);

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Member không tồn tại với ID: " + id));
        ParkingPlan plan = parkingPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new DataNotFoundException("Parking Plan không tồn tại với ID: " + id));
        if (member.getMemberStatus() == MemberStatus.CANCELLED) {
            throw new InvalidOperationException("Không thể gia hạn thẻ đã bị hủy. Vui lòng đăng ký mới.");
        }

        if (member.getMemberStatus() == MemberStatus.LOCKED) {
            throw new InvalidOperationException("Vui lòng mở khóa thẻ trước khi gia hạn");
        }

        // Calculate new expiry date
        LocalDateTime startDate;
        if (member.getMembershipExpiryDate() != null &&
                member.getMembershipExpiryDate().isAfter(LocalDateTime.now())) {
            startDate = member.getMembershipExpiryDate();
        } else {
            startDate = LocalDateTime.now();
            member.setMembershipStartDate(startDate);
        }

        LocalDateTime newExpiryDate = calculateExpiryDate(startDate, plan);
        BigDecimal fee = plan.getPrice();

        member.setParkingPlan(plan);
        member.setMembershipExpiryDate(newExpiryDate);
        member.setMembershipFee(fee);
        member.setMemberStatus(MemberStatus.ACTIVE);

        Member renewedMember = memberRepository.save(member);
        log.info("Renewed member with id: {} until {}", id, newExpiryDate);

        return MemberResponse.fromMember(renewedMember);
    }

    /**
     * Search members with multiple criteria
     */
    @Transactional(readOnly = true)
    public List<MemberResponse> searchMembers(MemberSearchRequest request) {
        log.info("Searching members with criteria: {}", request);

        // If searching by license plate, find user through vehicle first
        if (request.getLicensePlate() != null && !request.getLicensePlate().isEmpty()) {
            MemberResponse member = searchMemberByLicensePlate(request.getLicensePlate());
            if (member != null) {
                return List.of(member);
            }
            return List.of();
        }

        List<Member> members = memberRepository.searchMembers(
                null, // parkingLotId can be added to request
                request.getPhoneNumber(),
                request.getMemberCode(),
                request.getEmail(),
                request.getKeyword(),
                request.getMemberStatus());

        return MemberResponse.fromMembers(members);
    }

    /**
     * Search member by license plate
     */
    @Transactional(readOnly = true)
    public MemberResponse searchMemberByLicensePlate(String licensePlate) {
        log.info("Searching member by license plate: {}", licensePlate);

        Optional<Vehicle> vehicle = vehicleRepository.findByLicensePlate(licensePlate);
        if (vehicle.isPresent() && vehicle.get().getMember() != null) {
            Member member = vehicle.get().getMember();
            return MemberResponse.fromMember(member);
        }
        return null;
    }

    /**
     * Get membership statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMemberStatistics() {
        log.info("Getting member statistics");

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalMembers", memberRepository.count());
        stats.put("activeMembers", memberRepository.countByMemberStatus(MemberStatus.ACTIVE));
        stats.put("waitingPaymentMembers", memberRepository.countByMemberStatus(MemberStatus.WAITING_PAYMENT));
        stats.put("lockedMembers", memberRepository.countByMemberStatus(MemberStatus.LOCKED));
        stats.put("expiredMembers", memberRepository.countByMemberStatus(MemberStatus.EXPIRED));
        stats.put("cancelledMembers", memberRepository.countByMemberStatus(MemberStatus.CANCELLED));
        stats.put("pendingMembers", memberRepository.countByMemberStatus(MemberStatus.PENDING));

        // Count members by plan type instead of membership type
        stats.put("monthlyMembers", memberRepository.countByParkingPlanPriceUnit("MONTH"));
        stats.put("quarterlyMembers", memberRepository.countByParkingPlanPriceUnit("QUARTER"));
        stats.put("yearlyMembers", memberRepository.countByParkingPlanPriceUnit("YEAR"));

        // Members expiring in next 7 days
        List<Member> expiringMembers = memberRepository.findMembersExpiringBefore(
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7));
        stats.put("membersExpiringIn7Days", expiringMembers.size());

        return stats;
    }

    /**
     * Get members expiring soon
     */
    @Transactional(readOnly = true)
    public List<MemberResponse> getMembersExpiringSoon(int days) {
        log.info("Getting members expiring in {} days", days);
        List<Member> members = memberRepository.findMembersExpiringBefore(
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(days));
        return MemberResponse.fromMembers(members);
    }

    /**
     * Update expired members status (scheduled task)
     */
    @Transactional
    public int updateExpiredMembers() {
        log.info("Updating expired members status");
        List<Member> expiredMembers = memberRepository.findExpiredMembers(LocalDateTime.now());

        for (Member member : expiredMembers) {
            member.setMemberStatus(MemberStatus.EXPIRED);
        }

        memberRepository.saveAll(expiredMembers);
        log.info("Updated {} expired members", expiredMembers.size());

        return expiredMembers.size();
    }

    /**
     * Get membership fee based on type
     */

    // ============ PRIVATE HELPER METHODS ============

    /**
     * Generate unique member code
     */
    private String generateMemberCode() {
        String year = String.valueOf(Year.now().getValue());
        Long count = memberRepository.count() + 1;
        String code = String.format("MEM-%s-%05d", year, count);

        while (memberRepository.existsByMemberCode(code)) {
            count++;
            code = String.format("MEM-%s-%05d", year, count);
        }

        return code;
    }

    /**
     * Calculate membership expiry date based on parking plan
     */
    private LocalDateTime calculateExpiryDate(LocalDateTime startDate, ParkingPlan plan) {
        String priceUnit = plan.getPriceUnit().toUpperCase();
        switch (priceUnit) {
            case "HOUR":
                return startDate.plusHours(1);
            case "DAY":
                return startDate.plusDays(1);
            case "MONTH":
                return startDate.plusMonths(1);
            case "QUARTER":
                return startDate.plusMonths(3);
            case "YEAR":
                return startDate.plusYears(1);
            default:
                return startDate.plusMonths(1); // Default to monthly
        }
    }

    /**
     * Create vehicle for member
     */
    private void createVehicleForMember(Member member, String licensePlate, String vehicleType) {
        if (vehicleRepository.existsByLicensePlate(licensePlate)) {
            log.warn("Vehicle with license plate {} already exists", licensePlate);
            return;
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setMember(member);
        vehicle.setLicensePlate(licensePlate);
        vehicle.setVehicleType(vehicleType != null ? vehicleType : "CAR");
        vehicle.setCreatedAt(LocalDateTime.now());

        vehicleRepository.save(vehicle);
        log.info("Created vehicle {} for member {}", licensePlate, member.getMemberCode());
    }

    /**
     * Send pending notification email to user
     */
    private void sendPendingNotificationEmail(Member member) throws MessagingException {
        User user = member.getUser();
        String subject = "Đơn đăng ký thành viên đang chờ duyệt";
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Xin chào " + user.getFullname() + "!</h2>"
                + "<p style=\"font-size: 16px;\">Đơn đăng ký thành viên của bạn đã được ghi nhận và đang chờ duyệt.</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Thông tin đăng ký:</h3>"
                + "<p><strong>Mã đăng ký:</strong> " + member.getMemberCode() + "</p>"
                + "<p><strong>Loại gói:</strong> " + member.getParkingPlan().getName() + "</p>"
                + "<p><strong>Phí:</strong> " + member.getMembershipFee() + " VND</p>"
                + "<p><strong>Trạng thái:</strong> <span style=\"color: orange;\">Chờ duyệt</span></p>"
                + "</div>"
                + "<p style=\"margin-top: 20px;\">Chúng tôi sẽ thông báo cho bạn khi đơn được duyệt.</p>"
                + "</div>"
                + "</body>"
                + "</html>";

        emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
    }

    /**
     * Send welcome email to approved member
     */
    private void sendWelcomeEmail(Member member) throws MessagingException {
        User user = member.getUser();
        String subject = "Chào mừng bạn trở thành thành viên!";
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Chào mừng " + user.getFullname() + "!</h2>"
                + "<p style=\"font-size: 16px;\">Đơn đăng ký thành viên của bạn đã được <strong style=\"color: green;\">DUYỆT</strong>.</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Thông tin thẻ:</h3>"
                + "<p><strong>Mã thẻ:</strong> " + member.getMemberCode() + "</p>"
                + "<p><strong>Loại gói:</strong> " + member.getParkingPlan().getName() + "</p>"
                + "<p><strong>Ngày bắt đầu:</strong> " + member.getMembershipStartDate() + "</p>"
                + "<p><strong>Ngày hết hạn:</strong> " + member.getMembershipExpiryDate() + "</p>"
                + "</div>"
                + "<p style=\"margin-top: 20px;\">Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi!</p>"
                + "</div>"
                + "</body>"
                + "</html>";

        emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
    }

    /**
     * Send rejection email to member
     */
    private void sendRejectionEmail(Member member, String reason) throws MessagingException {
        User user = member.getUser();
        String subject = "Thông báo về đơn đăng ký thành viên";
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Xin chào " + user.getFullname() + "!</h2>"
                + "<p style=\"font-size: 16px;\">Rất tiếc, đơn đăng ký thành viên của bạn đã bị <strong style=\"color: red;\">TỪ CHỐI</strong>.</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Thông tin:</h3>"
                + "<p><strong>Mã đăng ký:</strong> " + member.getMemberCode() + "</p>"
                + "<p><strong>Lý do:</strong> " + (reason != null ? reason : "Không có lý do cụ thể") + "</p>"
                + "</div>"
                + "<p style=\"margin-top: 20px;\">Nếu có thắc mắc, vui lòng liên hệ với chúng tôi.</p>"
                + "</div>"
                + "</body>"
                + "</html>";

        emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
    }

    /**
     * Calculate fee for a parking plan
     */
    public BigDecimal calculatePlanFee(Long planId) throws DataNotFoundException {
        ParkingPlan plan = parkingPlanRepository.findById(planId)
                .orElseThrow(() -> new DataNotFoundException("Parking plan not found"));
        return plan.getPrice();
    }
}
