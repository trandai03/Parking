package com.project.parking.service;

import com.project.parking.enums.InvoiceStatus;
import com.project.parking.enums.MemberStatus;
import com.project.parking.enums.PaymentStatus;
import com.project.parking.exceptions.DataNotFoundException;
import com.project.parking.model.Invoice;
import com.project.parking.model.Member;
import com.project.parking.model.PaymentHistory;
import com.project.parking.repository.InvoiceRepository;
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
import java.util.UUID;

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

    private final InvoiceRepository invoiceRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final MemberRepository memberRepository;
    private final InvoiceService invoiceService;
    private final EmailService emailService;
    private final RestTemplate restTemplate;

    /**
     * Tạo thanh toán MoMo cho member
     * Lấy Invoice UNPAID → Tạo PaymentHistory → Gọi MoMo API
     */
    @Transactional
    public PaymentHistory createMemberPayment(Long invoiceId) throws DataNotFoundException {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new DataNotFoundException("Invoice không tồn tại với ID: " + invoiceId));

        if (invoice.getStatus() != InvoiceStatus.UNPAID) {
            throw new IllegalStateException("Invoice không ở trạng thái chưa thanh toán");
        }

        // Kiểm tra xem đã có payment pending cho invoice này chưa
        // paymentHistoryRepository
        //         .findFirstByInvoiceIdAndPaymentStatusOrderByCreatedAtDesc(invoice.getId(), PaymentStatus.PENDING)
        //         .ifPresent(existingPayment -> {
        //             throw new IllegalStateException("Đã có giao dịch thanh toán đang chờ xử lý");
        //         });

        String orderId = generateOrderId();

        // Tạo payment history record
        PaymentHistory paymentHistory = PaymentHistory.builder()
                .invoiceId(invoice.getId())
                .paymentMethod("MOMO")
                .paymentStatus(PaymentStatus.PENDING)
                .orderId(orderId)
                .build();

        // Gọi MoMo API
        String payUrl = payWithMoMo(orderId, invoice.getAmount(), invoice.getMemberId());
        paymentHistory.setPaymentUrl(payUrl);

        PaymentHistory savedPayment = paymentHistoryRepository.save(paymentHistory);
        log.info("Created payment for member {}: orderId={}, amount={}",
                invoice.getMemberId(), orderId, invoice.getAmount());

        return savedPayment;
    }

    /**
     * Tạo URL thanh toán MoMo
     */
    public String payWithMoMo(String orderId, BigDecimal amount, Long memberId) {
        final String ipnUrlFinal = String.format("%s/%d", ipnUrl, memberId);
        String orderInfo = "Thanh toan phi thanh vien";
        String extraData = "";
        String requestId = UUID.randomUUID().toString().replace("-", "");
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

        // Verify signature từ MoMo
        if (!verifyMoMoSignature(payload)) {
            log.error("Invalid MoMo signature for member {}", memberId);
            throw new SecurityException("Chữ ký MoMo không hợp lệ");
        }

        String orderId = (String) payload.get("orderId");
        Integer resultCode = (Integer) payload.get("resultCode");

        PaymentHistory paymentHistory = paymentHistoryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch với orderId: " + orderId));

        // Lấy Invoice từ PaymentHistory
        Invoice invoice = invoiceRepository.findById(paymentHistory.getInvoiceId())
                .orElseThrow(
                        () -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + paymentHistory.getInvoiceId()));

        // Validate memberId khớp với invoice
        if (!invoice.getMemberId().equals(memberId)) {
            log.error("MemberId mismatch: URL memberId={}, invoice memberId={}",
                    memberId, invoice.getMemberId());
            throw new SecurityException("MemberId không khớp với hóa đơn");
        }

        // Kiểm tra payment chưa được xử lý
        if (paymentHistory.getPaymentStatus() != PaymentStatus.PENDING) {
            log.warn("Payment {} already processed with status {}", orderId, paymentHistory.getPaymentStatus());
            return;
        }

        if (resultCode == 0) {
            // Thanh toán thành công
            paymentHistory.setPaymentStatus(PaymentStatus.COMPLETED);
            paymentHistory.setPaymentTime(LocalDateTime.now());
            paymentHistoryRepository.save(paymentHistory);

            // Cập nhật Invoice
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidAt(LocalDateTime.now());
            invoiceRepository.save(invoice);

            // Cập nhật trạng thái member
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new RuntimeException("Member không tồn tại"));
            member.setMemberStatus(MemberStatus.ACTIVE);
            LocalDateTime startDate = LocalDateTime.now();
            member.setMembershipStartDate(startDate);

            // Tính toán ngày hết hạn dựa trên plan duration
            Integer durationMonths = member.getParkingPlan().getDurationMonths();
            if (durationMonths == null || durationMonths <= 0) {
                durationMonths = 1;
            }
            member.setMembershipExpiryDate(startDate.plusMonths(durationMonths));
            memberRepository.save(member);

            // Gửi email xác nhận
            sendPaymentConfirmationEmail(member, invoice);

            log.info("Payment completed for member {}, expiry: {}", memberId, member.getMembershipExpiryDate());
        } else {
            // Thanh toán thất bại
            paymentHistory.setPaymentStatus(PaymentStatus.FAILED);
            paymentHistoryRepository.save(paymentHistory);

            log.warn("Payment failed for member {}: resultCode={}", memberId, resultCode);
        }
    }

    /**
     * Verify chữ ký IPN từ MoMo
     */
    private boolean verifyMoMoSignature(Map<String, Object> payload) {
        try {
            String receivedSignature = String.valueOf(payload.get("signature"));
            if (receivedSignature == null || receivedSignature.isEmpty()) {
                return false;
            }

            String rawHash = "accessKey=" + accessKey +
                    "&amount=" + payload.get("amount") +
                    "&extraData=" + (payload.get("extraData") != null ? payload.get("extraData") : "") +
                    "&message=" + payload.get("message") +
                    "&orderId=" + payload.get("orderId") +
                    "&orderInfo=" + payload.get("orderInfo") +
                    "&orderType=" + payload.get("orderType") +
                    "&partnerCode=" + payload.get("partnerCode") +
                    "&payType=" + payload.get("payType") +
                    "&requestId=" + payload.get("requestId") +
                    "&responseTime=" + payload.get("responseTime") +
                    "&resultCode=" + payload.get("resultCode") +
                    "&transId=" + payload.get("transId");

            String expectedSignature = hmacSHA256(rawHash, secretKey);
            return expectedSignature.equals(receivedSignature);
        } catch (Exception e) {
            log.error("Error verifying MoMo signature", e);
            return false;
        }
    }

    /**
     * Xác nhận thanh toán thủ công (cho admin)
     */
    @Transactional
    public PaymentHistory confirmPaymentManually(Long invoiceId, String paymentMethod, Long processedByUserId)
            throws DataNotFoundException {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy hóa đơn với ID: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("Hóa đơn đã được thanh toán");
        }

        // Tạo PaymentHistory mới cho thanh toán thủ công
        PaymentHistory paymentHistory = PaymentHistory.builder()
                .invoiceId(invoiceId)
                .paymentMethod(paymentMethod)
                .paymentStatus(PaymentStatus.COMPLETED)
                .orderId(generateOrderId())
                .processedByUserId(processedByUserId)
                .paymentTime(LocalDateTime.now())
                .build();

        paymentHistoryRepository.save(paymentHistory);

        // Cập nhật Invoice
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        invoiceRepository.save(invoice);

        // Nếu là invoice membership, cập nhật member
        if (invoice.getMemberId() != null) {
            Member member = memberRepository.findById(invoice.getMemberId())
                    .orElseThrow(() -> new DataNotFoundException("Member không tồn tại"));

            member.setMemberStatus(MemberStatus.ACTIVE);
            LocalDateTime startDate = LocalDateTime.now();
            member.setMembershipStartDate(startDate);

            Integer durationMonths = member.getParkingPlan().getDurationMonths();
            if (durationMonths == null || durationMonths <= 0) {
                durationMonths = 1;
            }
            member.setMembershipExpiryDate(startDate.plusMonths(durationMonths));
            memberRepository.save(member);

            sendPaymentConfirmationEmail(member, invoice);

            log.info("Payment confirmed manually for member {}: invoiceId={}",
                    invoice.getMemberId(), invoiceId);
        }

        return paymentHistory;
    }

    /**
     * Lấy lịch sử thanh toán của member (qua Invoice JOIN)
     */
    public List<Object[]> getPaymentHistoryByMember(Long memberId) {
        return paymentHistoryRepository.findPaymentHistoryByMemberId(memberId);
    }

    /**
     * Lấy thanh toán đang pending của invoice
     */
    public PaymentHistory getPendingPayment(Long invoiceId) throws DataNotFoundException {
        return paymentHistoryRepository
                .findFirstByInvoiceIdAndPaymentStatusOrderByCreatedAtDesc(invoiceId, PaymentStatus.PENDING)
                .orElseThrow(() -> new DataNotFoundException("Không có giao dịch đang chờ thanh toán"));
    }

    /**
     * Kiểm tra và khóa các member không thanh toán quá hạn
     */
    @Transactional
    public int lockOverdueMembers() {
        List<Invoice> overdueInvoices = invoiceRepository.findOverdueInvoices(
                InvoiceStatus.UNPAID, LocalDateTime.now());

        int lockedCount = 0;
        for (Invoice invoice : overdueInvoices) {
            if (invoice.getMemberId() == null)
                continue;

            Member member = memberRepository.findById(invoice.getMemberId()).orElse(null);
            if (member == null)
                continue;

            if (member.getMemberStatus() == MemberStatus.WAITING_PAYMENT) {
                member.setMemberStatus(MemberStatus.LOCKED);
                member.setLockedAt(LocalDateTime.now());
                member.setLockReason("Không thanh toán trong thời hạn quy định (5 ngày)");
                memberRepository.save(member);

                // Đánh dấu invoice là OVERDUE
                invoice.setStatus(InvoiceStatus.OVERDUE);
                invoiceRepository.save(invoice);

                // Đánh dấu các payment pending là FAILED
                paymentHistoryRepository
                        .findFirstByInvoiceIdAndPaymentStatusOrderByCreatedAtDesc(invoice.getId(),
                                PaymentStatus.PENDING)
                        .ifPresent(ph -> {
                            ph.setPaymentStatus(PaymentStatus.FAILED);
                            paymentHistoryRepository.save(ph);
                        });

                sendLockNotificationEmail(member);

                lockedCount++;
                log.info("Locked member {} due to overdue invoice {}", member.getId(), invoice.getId());
            }
        }

        return lockedCount;
    }

    /**
     * Gửi email nhắc nhở thanh toán
     */
    public void sendPaymentReminderEmail(Member member, Invoice invoice) {
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
                + "<p><strong>Mã hóa đơn:</strong> " + invoice.getInvoiceCode() + "</p>"
                + "<p><strong>Số tiền:</strong> " + invoice.getAmount() + " VND</p>"
                + "<p><strong>Hạn thanh toán:</strong> " + invoice.getPaymentDeadline() + "</p>"
                + "</div>"
                + "<p style=\"color: red;\"><strong>Lưu ý:</strong> Nếu không thanh toán trước hạn, thẻ của bạn sẽ bị khóa.</p>"
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
    private void sendPaymentConfirmationEmail(Member member, Invoice invoice) {
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
                + "<h3>Thông tin:</h3>"
                + "<p><strong>Mã thẻ:</strong> " + member.getMemberCode() + "</p>"
                + "<p><strong>Mã hóa đơn:</strong> " + invoice.getInvoiceCode() + "</p>"
                + "<p><strong>Gói:</strong> " + member.getParkingPlan().getName() + "</p>"
                + "<p><strong>Số tiền:</strong> " + invoice.getAmount() + " VND</p>"
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
        return "PAY" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
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
