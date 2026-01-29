package com.project.parking.controller;

import com.project.parking.model.PaymentHistory;
import com.project.parking.response.Response;
import com.project.parking.service.PaymentService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.v1.prefix}/payments")
@Tag(name = "Payment", description = "APIs quản lý thanh toán")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Tạo thanh toán cho member",
               description = "Tạo link thanh toán MoMo cho member sau khi được duyệt")
    @PostMapping("/member/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MEMBER', 'USER')")
    public ResponseEntity<Response> createMemberPayment(
            @Parameter(description = "ID của member") @PathVariable Long memberId) {
        try {
            log.info("Creating payment for member: {}", memberId);
            PaymentHistory payment = paymentService.createMemberPayment(memberId);
            return ResponseEntity.ok(new Response("success", "Tạo link thanh toán thành công", Map.of(
                    "paymentId", payment.getId(),
                    "paymentUrl", payment.getPaymentUrl(),
                    "amount", payment.getAmount(),
                    "deadline", payment.getPaymentDeadline()
            )));
        } catch (Exception e) {
            log.error("Error creating payment for member: {}", memberId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Hidden
    @Operation(summary = "MoMo IPN Callback",
               description = "Callback từ MoMo sau khi thanh toán")
    @PostMapping("/ipn/{memberId}")
    public ResponseEntity<Response> handleMoMoIPN(
            @PathVariable Long memberId,
            @RequestBody Map<String, Object> payload) {
        try {
            log.info("MoMo IPN received for member: {}", memberId);
            log.info("Payload: {}", payload);

            paymentService.handleMoMoIPN(memberId, payload);

            return ResponseEntity.ok(new Response("success", "IPN processed successfully", null));
        } catch (Exception e) {
            log.error("Error processing MoMo IPN for member: {}", memberId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Xác nhận thanh toán thủ công",
               description = "Admin xác nhận thanh toán thủ công (tiền mặt, chuyển khoản)")
    @PostMapping("/{paymentId}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> confirmPaymentManually(
            @Parameter(description = "ID của payment") @PathVariable Long paymentId,
            @Parameter(description = "Phương thức thanh toán") @RequestParam(defaultValue = "CASH") String paymentMethod) {
        try {
            log.info("Confirming payment manually: paymentId={}, method={}", paymentId, paymentMethod);
            PaymentHistory payment = paymentService.confirmPaymentManually(paymentId, paymentMethod);
            return ResponseEntity.ok(new Response("success", "Xác nhận thanh toán thành công", payment));
        } catch (Exception e) {
            log.error("Error confirming payment: {}", paymentId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Lấy lịch sử thanh toán của member",
               description = "Lấy danh sách các giao dịch thanh toán của member")
    @GetMapping("/member/{memberId}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MEMBER')")
    public ResponseEntity<Response> getPaymentHistory(
            @Parameter(description = "ID của member") @PathVariable Long memberId) {
        try {
            List<PaymentHistory> history = paymentService.getPaymentHistory(memberId);
            return ResponseEntity.ok(new Response("success", "Lấy lịch sử thanh toán thành công", history));
        } catch (Exception e) {
            log.error("Error getting payment history for member: {}", memberId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Lấy thanh toán đang chờ của member",
               description = "Lấy thông tin giao dịch đang chờ thanh toán của member")
    @GetMapping("/member/{memberId}/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MEMBER', 'USER')")
    public ResponseEntity<Response> getPendingPayment(
            @Parameter(description = "ID của member") @PathVariable Long memberId) {
        try {
            PaymentHistory payment = paymentService.getPendingPayment(memberId);
            return ResponseEntity.ok(new Response("success", "Lấy thông tin thanh toán thành công", Map.of(
                    "paymentId", payment.getId(),
                    "paymentUrl", payment.getPaymentUrl(),
                    "amount", payment.getAmount(),
                    "deadline", payment.getPaymentDeadline(),
                    "status", payment.getPaymentStatus()
            )));
        } catch (Exception e) {
            log.error("Error getting pending payment for member: {}", memberId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Kiểm tra và khóa member quá hạn thanh toán",
               description = "Admin chạy thủ công để khóa các member không thanh toán trong 5 ngày")
    @PostMapping("/check-overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> checkOverduePayments() {
        try {
            log.info("Checking overdue payments...");
            int lockedCount = paymentService.lockOverdueMembers();
            return ResponseEntity.ok(new Response("success", 
                    "Đã khóa " + lockedCount + " member do quá hạn thanh toán", 
                    Map.of("lockedCount", lockedCount)));
        } catch (Exception e) {
            log.error("Error checking overdue payments", e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Gửi email nhắc nhở thanh toán",
               description = "Gửi email nhắc nhở cho member chưa thanh toán")
    @PostMapping("/member/{memberId}/remind")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> sendPaymentReminder(
            @Parameter(description = "ID của member") @PathVariable Long memberId) {
        try {
            PaymentHistory payment = paymentService.getPendingPayment(memberId);
            paymentService.sendPaymentReminderEmail(payment.getMember(), payment);
            return ResponseEntity.ok(new Response("success", "Đã gửi email nhắc nhở thanh toán", null));
        } catch (Exception e) {
            log.error("Error sending payment reminder for member: {}", memberId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }
}
