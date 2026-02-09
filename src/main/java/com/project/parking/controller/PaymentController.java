package com.project.parking.controller;

import com.project.parking.model.Invoice;
import com.project.parking.model.PaymentHistory;
import com.project.parking.response.Response;
import com.project.parking.service.InvoiceService;
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
    private final InvoiceService invoiceService;

    @Operation(summary = "Tạo thanh toán cho member", description = "Tạo link thanh toán MoMo cho member sau khi được duyệt")
    @PostMapping("/invoice/{invoiceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MEMBER', 'USER')")
    public ResponseEntity<Response> createMemberPayment(
            @Parameter(description = "ID của invoice") @PathVariable Long invoiceId) {
        try {
            log.info("Creating payment for invoice: {}", invoiceId);
            PaymentHistory payment = paymentService.createMemberPayment(invoiceId);
            Invoice invoice = invoiceService.getInvoiceById(invoiceId);

            return ResponseEntity.ok(new Response("success", "Tạo link thanh toán thành công", Map.of(
                    "paymentId", payment.getId(),
                    "invoiceId", invoice.getId(),
                    "invoiceCode", invoice.getInvoiceCode(),
                    "paymentUrl", payment.getPaymentUrl(),
                    "amount", invoice.getAmount(),
                    "deadline", invoice.getPaymentDeadline())));
        } catch (Exception e) {
            log.error("Error creating payment for invoice: {}", invoiceId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "MoMo IPN Callback", description = "Callback từ MoMo sau khi thanh toán")
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

    @Operation(summary = "Xác nhận thanh toán thủ công", description = "Admin xác nhận thanh toán thủ công (tiền mặt, chuyển khoản)")
    @PostMapping("/invoice/{invoiceId}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> confirmPaymentManually(
            @Parameter(description = "ID của invoice") @PathVariable Long invoiceId,
            @Parameter(description = "Phương thức thanh toán") @RequestParam(defaultValue = "CASH") String paymentMethod,
            @Parameter(description = "ID của user xử lý") @RequestParam(required = false) Long processedByUserId) {
        try {
            log.info("Confirming payment manually: invoiceId={}, method={}", invoiceId, paymentMethod);
            PaymentHistory payment = paymentService.confirmPaymentManually(invoiceId, paymentMethod, processedByUserId);
            return ResponseEntity.ok(new Response("success", "Xác nhận thanh toán thành công", payment));
        } catch (Exception e) {
            log.error("Error confirming payment for invoice: {}", invoiceId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Lấy danh sách hóa đơn của member", description = "Lấy tất cả hóa đơn của member")
    @GetMapping("/invoices/member/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MEMBER')")
    public ResponseEntity<Response> getInvoicesByMember(
            @Parameter(description = "ID của member") @PathVariable Long memberId) {
        try {
            List<Invoice> invoices = invoiceService.getInvoicesByMember(memberId);
            return ResponseEntity.ok(new Response("success", "Lấy danh sách hóa đơn thành công", invoices));
        } catch (Exception e) {
            log.error("Error getting invoices for member: {}", memberId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Lấy hóa đơn chưa thanh toán của member", description = "Lấy hóa đơn UNPAID của member")
    @GetMapping("/invoices/member/{memberId}/unpaid")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MEMBER', 'USER')")
    public ResponseEntity<Response> getUnpaidInvoice(
            @Parameter(description = "ID của member") @PathVariable Long memberId) {
        try {
            List<Invoice> invoices = invoiceService.getUnpaidInvoiceByMember(memberId);
            return ResponseEntity.ok(new Response("success", "Lấy hóa đơn thành công", invoices));
        } catch (Exception e) {
            log.error("Error getting unpaid invoice for member: {}", memberId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Lấy lịch sử thanh toán của member", description = "Lấy danh sách các giao dịch thanh toán của member")
    @GetMapping("/member/{memberId}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MEMBER')")
    public ResponseEntity<Response> getPaymentHistory(
            @Parameter(description = "ID của member") @PathVariable Long memberId) {
        try {
            List<Object[]> history = paymentService.getPaymentHistoryByMember(memberId);
            return ResponseEntity.ok(new Response("success", "Lấy lịch sử thanh toán thành công", history));
        } catch (Exception e) {
            log.error("Error getting payment history for member: {}", memberId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Lấy lịch sử thanh toán của invoice", description = "Lấy các lần thanh toán cho 1 hóa đơn cụ thể")
    @GetMapping("/invoices/{invoiceId}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MEMBER')")
    public ResponseEntity<Response> getPaymentHistoryByInvoice(
            @Parameter(description = "ID của invoice") @PathVariable Long invoiceId) {
        try {
            Invoice invoice = invoiceService.getInvoiceById(invoiceId);
            // TODO: Add method to get payment history by invoice ID
            return ResponseEntity.ok(new Response("success", "Lấy lịch sử thanh toán thành công",
                    Map.of("invoice", invoice)));
        } catch (Exception e) {
            log.error("Error getting payment history for invoice: {}", invoiceId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Kiểm tra và khóa member quá hạn thanh toán", description = "Admin chạy thủ công để khóa các member không thanh toán trong 5 ngày")
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

    // @Operation(summary = "Gửi email nhắc nhở thanh toán", description = "Gửi email nhắc nhở cho member chưa thanh toán")
    // @PostMapping("/member/{memberId}/remind")
    // @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    // public ResponseEntity<Response> sendPaymentReminder(
    //         @Parameter(description = "ID của member") @PathVariable Long memberId) {
    //     try {
    //         List<Invoice> invoices = invoiceService.getUnpaidInvoiceByMember(memberId);
    //         // Get member from repository
    //         // For now, we'll use the invoice service pattern
    //         return ResponseEntity.ok(new Response("success", "Đã gửi email nhắc nhở thanh toán", null));
    //     } catch (Exception e) {
    //         log.error("Error sending payment reminder for member: {}", memberId, e);
    //         return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
    //     }
    // }
}
