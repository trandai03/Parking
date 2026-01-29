package com.project.parking.service;

import com.project.parking.enums.MemberStatus;
import com.project.parking.enums.PaymentStatus;
import com.project.parking.exceptions.DataNotFoundException;
import com.project.parking.model.Member;
import com.project.parking.model.PaymentHistory;
import com.project.parking.repository.MemberRepository;
import com.project.parking.repository.PaymentHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    @Value("${momo.endpoint}")
    private String endPoint;

    @Value("${momo.partnerCode}")
    private String partnerCode;

    @Value("${momo.accessKey}")
    private String accessKey;

    @Value("${momo.secretKey}")
    private String secretKey;

    @Value("${ipUrl}")
    private String ipnUrl;

    @Value("${redirectUrl}")
    private String redirectUrl;

    private final PaymentHistoryRepository paymentHistoryRepository;
    private final MemberRepository memberRepository;
    private final EmailService emailService;

    /**
     * Tạo thanh toán MoMo cho member
     */
    @Transactional
    public PaymentHistory createMemberPayment(Long memberId) throws DataNotFoundException {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new DataNotFoundException("Member không tồn tại với ID: " + memberId));

        if (member.getMemberStatus() != MemberStatus.WAITING_PAYMENT) {
            throw new IllegalStateException("Member không ở trạng thái chờ thanh toán");
        }

        // Kiểm tra xem đã có payment pending chưa
        paymentHistoryRepository.findFirstByMemberIdAndPaymentStatusOrderByCreatedAtDesc(memberId, PaymentStatus.PENDING)
                .ifPresent(existingPayment -> {
                    throw new IllegalStateException("Đã có giao dịch thanh toán đang chờ xử lý");
                });

        BigDecimal amount = member.getMembershipFee();
        String orderId = generateOrderId();
        
        // Tạo payment history record
        PaymentHistory paymentHistory = PaymentHistory.builder()
                .member(member)
                .parkingPlan(member.getParkingPlan())
                .amount(amount)
                .paymentMethod("MOMO")
                .paymentStatus(PaymentStatus.PENDING)
                .orderId(orderId)
                .description("Thanh toán phí thành viên - " + member.getMemberCode())
                .paymentDeadline(LocalDateTime.now().plusDays(5)) // 5 ngày để thanh toán
                .build();

        // Gọi MoMo API
        String payUrl = payWithMoMo(orderId, amount, memberId);
        paymentHistory.setPaymentUrl(payUrl);

        PaymentHistory savedPayment = paymentHistoryRepository.save(paymentHistory);
        log.info("Created payment for member {}: orderId={}, amount={}", memberId, orderId, amount);

        return savedPayment;
    }

    /**
     * Tạo URL thanh toán MoMo
     */
    public String payWithMoMo(String orderId, BigDecimal amount, Long memberId) {
        final String ipnUrlFinal = String.format("%s/%d", ipnUrl, memberId);
        String orderInfo = "Thanh toan phi thanh vien";
        String extraData = "";
        String requestId = String.valueOf(System.currentTimeMillis() + new Random().nextInt(999 - 111 + 1) + 111);
        String requestType = "captureWallet";

        String rawHash = "accessKey=" + accessKey +
                "&amount=" + amount.longValue() +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrlFinal +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;

        String signature = hmacSHA256(rawHash, secretKey);

        Map<String, Object> data = new HashMap<>();
        data.put("partnerCode", partnerCode);
        data.put("partnerName", "Parking Management");
        data.put("storeId", "ParkingStore");
        data.put("requestId", requestId);
        data.put("amount", amount.longValue());
        data.put("orderId", orderId);
        data.put("orderInfo", orderInfo);
        data.put("redirectUrl", redirectUrl);
        data.put("ipnUrl", ipnUrlFinal);
        data.put("lang", "vi");
        data.put("extraData", extraData);
        data.put("requestType", requestType);
        data.put("signature", signature);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(endPoint, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.get("payUrl") != null) {
                return responseBody.get("payUrl").toString();
            }
            throw new RuntimeException("Không thể tạo URL thanh toán MoMo");
        } catch (Exception e) {
            log.error("Error creating MoMo payment URL", e);
            throw new RuntimeException("Lỗi khi gọi MoMo API: " + e.getMessage());
        }
    }

    /**
     * Xử lý callback IPN từ MoMo
     */
    @Transactional
    public void handleMoMoIPN(Long memberId, Map<String, Object> payload) {
        log.info("Processing MoMo IPN for member {}: {}", memberId, payload);

        String orderId = (String) payload.get("orderId");
        Integer resultCode = (Integer) payload.get("resultCode");

        PaymentHistory paymentHistory = paymentHistoryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch với orderId: " + orderId));

        if (resultCode == 0) {
            // Thanh toán thành công
            paymentHistory.setPaymentStatus(PaymentStatus.COMPLETED);
            paymentHistory.setPaymentTime(LocalDateTime.now());
            paymentHistoryRepository.save(paymentHistory);

            // Cập nhật trạng thái member
            Member member = paymentHistory.getMember();
            member.setMemberStatus(MemberStatus.ACTIVE);
            member.setMembershipStartDate(LocalDateTime.now());
            memberRepository.save(member);

            // Gửi email xác nhận
            sendPaymentConfirmationEmail(member, paymentHistory);

            log.info("Payment completed for member {}", memberId);
        } else {
            // Thanh toán thất bại
            paymentHistory.setPaymentStatus(PaymentStatus.FAILED);
            paymentHistoryRepository.save(paymentHistory);

            log.warn("Payment failed for member {}: resultCode={}", memberId, resultCode);
        }
    }

    /**
     * Xác nhận thanh toán thủ công (cho admin)
     */
    @Transactional
    public PaymentHistory confirmPaymentManually(Long paymentId, String paymentMethod) throws DataNotFoundException {
        PaymentHistory paymentHistory = paymentHistoryRepository.findById(paymentId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy giao dịch với ID: " + paymentId));

        if (paymentHistory.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Giao dịch đã được xử lý");
        }

        paymentHistory.setPaymentStatus(PaymentStatus.COMPLETED);
        paymentHistory.setPaymentMethod(paymentMethod);
        paymentHistory.setPaymentTime(LocalDateTime.now());
        paymentHistoryRepository.save(paymentHistory);

        // Cập nhật trạng thái member
        Member member = paymentHistory.getMember();
        member.setMemberStatus(MemberStatus.ACTIVE);
        member.setMembershipStartDate(LocalDateTime.now());
        memberRepository.save(member);

        // Gửi email xác nhận
        sendPaymentConfirmationEmail(member, paymentHistory);

        log.info("Payment confirmed manually for member {}: paymentId={}", member.getId(), paymentId);

        return paymentHistory;
    }

    /**
     * Lấy lịch sử thanh toán của member
     */
    public List<PaymentHistory> getPaymentHistory(Long memberId) {
        return paymentHistoryRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    /**
     * Lấy thanh toán đang pending của member
     */
    public PaymentHistory getPendingPayment(Long memberId) throws DataNotFoundException {
        return paymentHistoryRepository.findFirstByMemberIdAndPaymentStatusOrderByCreatedAtDesc(memberId, PaymentStatus.PENDING)
                .orElseThrow(() -> new DataNotFoundException("Không có giao dịch đang chờ thanh toán"));
    }

    /**
     * Kiểm tra và khóa các member không thanh toán quá hạn
     */
    @Transactional
    public int lockOverdueMembers() {
        List<PaymentHistory> overduePayments = paymentHistoryRepository.findOverduePayments(
                PaymentStatus.PENDING, LocalDateTime.now());

        int lockedCount = 0;
        for (PaymentHistory payment : overduePayments) {
            Member member = payment.getMember();
            if (member.getMemberStatus() == MemberStatus.WAITING_PAYMENT) {
                member.setMemberStatus(MemberStatus.LOCKED);
                member.setLockedAt(LocalDateTime.now());
                member.setLockReason("Không thanh toán trong thời hạn quy định (5 ngày)");
                memberRepository.save(member);

                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentHistoryRepository.save(payment);

                // Gửi email thông báo khóa
                sendLockNotificationEmail(member);

                lockedCount++;
                log.info("Locked member {} due to overdue payment", member.getId());
            }
        }

        return lockedCount;
    }

    /**
     * Gửi email nhắc nhở thanh toán
     */
    public void sendPaymentReminderEmail(Member member, PaymentHistory payment) {
        String subject = "Nhắc nhở thanh toán - Parking Management";
        String htmlMessage = "<!DOCTYPE html>"
                + "<html>"
                + "<head><meta charset=\"UTF-8\"></head>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">"
                + "<h2 style=\"color: #ff9800;\">Nhắc nhở thanh toán</h2>"
                + "<p>Xin chào <strong>" + member.getUser().getFullname() + "</strong>,</p>"
                + "<p>Chúng tôi nhận thấy bạn chưa hoàn thành thanh toán phí thành viên.</p>"
                + "<div style=\"background-color: #fff3e0; padding: 20px; border-radius: 5px; margin: 20px 0;\">"
                + "<p><strong>Mã thẻ:</strong> " + member.getMemberCode() + "</p>"
                + "<p><strong>Số tiền:</strong> " + payment.getAmount() + " VND</p>"
                + "<p><strong>Hạn thanh toán:</strong> " + payment.getPaymentDeadline() + "</p>"
                + "</div>"
                + "<p style=\"color: red;\"><strong>Lưu ý:</strong> Nếu không thanh toán trước hạn, thẻ của bạn sẽ bị khóa.</p>"
                + "<p><a href=\"" + payment.getPaymentUrl() + "\" style=\"background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;\">Thanh toán ngay</a></p>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(member.getUser().getEmail(), subject, htmlMessage);
            log.info("Sent payment reminder email to member {}", member.getId());
        } catch (Exception e) {
            log.error("Failed to send payment reminder email to member {}", member.getId(), e);
        }
    }

    /**
     * Gửi email xác nhận thanh toán thành công
     */
    private void sendPaymentConfirmationEmail(Member member, PaymentHistory payment) {
        String subject = "Thanh toán thành công - Parking Management";
        String htmlMessage = "<!DOCTYPE html>"
                + "<html>"
                + "<head><meta charset=\"UTF-8\"></head>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">"
                + "<h2 style=\"color: #4CAF50;\">Thanh toán thành công!</h2>"
                + "<p>Xin chào <strong>" + member.getUser().getFullname() + "</strong>,</p>"
                + "<p>Cảm ơn bạn đã thanh toán. Thẻ thành viên của bạn đã được kích hoạt.</p>"
                + "<div style=\"background-color: #e8f5e9; padding: 20px; border-radius: 5px; margin: 20px 0;\">"
                + "<h3>Thông tin thẻ:</h3>"
                + "<p><strong>Mã thẻ:</strong> " + member.getMemberCode() + "</p>"
                + "<p><strong>Gói:</strong> " + member.getParkingPlan().getName() + "</p>"
                + "<p><strong>Số tiền:</strong> " + payment.getAmount() + " VND</p>"
                + "<p><strong>Ngày bắt đầu:</strong> " + member.getMembershipStartDate() + "</p>"
                + "<p><strong>Ngày hết hạn:</strong> " + member.getMembershipExpiryDate() + "</p>"
                + "</div>"
                + "<p>Chúc bạn sử dụng dịch vụ vui vẻ!</p>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(member.getUser().getEmail(), subject, htmlMessage);
            log.info("Sent payment confirmation email to member {}", member.getId());
        } catch (Exception e) {
            log.error("Failed to send payment confirmation email to member {}", member.getId(), e);
        }
    }

    /**
     * Gửi email thông báo khóa thẻ
     */
    private void sendLockNotificationEmail(Member member) {
        String subject = "Thẻ thành viên đã bị khóa - Parking Management";
        String htmlMessage = "<!DOCTYPE html>"
                + "<html>"
                + "<head><meta charset=\"UTF-8\"></head>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">"
                + "<h2 style=\"color: #f44336;\">Thẻ thành viên đã bị khóa</h2>"
                + "<p>Xin chào <strong>" + member.getUser().getFullname() + "</strong>,</p>"
                + "<p>Thẻ thành viên của bạn đã bị khóa do không thanh toán trong thời hạn quy định.</p>"
                + "<div style=\"background-color: #ffebee; padding: 20px; border-radius: 5px; margin: 20px 0;\">"
                + "<p><strong>Mã thẻ:</strong> " + member.getMemberCode() + "</p>"
                + "<p><strong>Lý do:</strong> Không thanh toán trong thời hạn quy định (5 ngày)</p>"
                + "</div>"
                + "<p>Vui lòng liên hệ quản trị viên để được hỗ trợ mở khóa.</p>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(member.getUser().getEmail(), subject, htmlMessage);
            log.info("Sent lock notification email to member {}", member.getId());
        } catch (Exception e) {
            log.error("Failed to send lock notification email to member {}", member.getId(), e);
        }
    }

    /**
     * Generate unique order ID
     */
    private String generateOrderId() {
        return "MEM" + System.currentTimeMillis() + new Random().nextInt(1000);
    }

    /**
     * HMAC SHA256 signature
     */
    private String hmacSHA256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : rawHmac) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC SHA-256", e);
        }
    }
}
