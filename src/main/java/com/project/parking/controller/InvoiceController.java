package com.project.parking.controller;

import com.project.parking.enums.InvoiceStatus;
import com.project.parking.enums.InvoiceType;
import com.project.parking.model.Invoice;
import com.project.parking.response.Response;
import com.project.parking.response.invoice.InvoiceResponse;
import com.project.parking.service.InvoiceService;
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
@RequestMapping("${api.v1.prefix}/invoices")
@Tag(name = "Invoice", description = "APIs quản lý hóa đơn")
@Slf4j
public class InvoiceController {

    private final InvoiceService invoiceService;

    // ============ PUBLIC/MEMBER ENDPOINTS ============

    @Operation(summary = "Lấy hóa đơn theo ID", description = "Lấy thông tin chi tiết của một hóa đơn")
    @GetMapping("/{invoiceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MEMBER')")
    public ResponseEntity<Response> getInvoiceById(
            @Parameter(description = "ID của hóa đơn") @PathVariable Long invoiceId) {
        try {
            Invoice invoice = invoiceService.getInvoiceById(invoiceId);
            return ResponseEntity.ok(new Response("success", "Lấy hóa đơn thành công", InvoiceResponse.from(invoice)));
        } catch (Exception e) {
            log.error("Error getting invoice: {}", invoiceId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Lấy hóa đơn theo mã hóa đơn", description = "Tìm kiếm hóa đơn bằng mã invoice code")
    @GetMapping("/code/{invoiceCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MEMBER')")
    public ResponseEntity<Response> getInvoiceByCode(
            @Parameter(description = "Mã hóa đơn") @PathVariable String invoiceCode) {
        try {
            List<Invoice> invoices = invoiceService.getInvoiceByCode(invoiceCode);
            return ResponseEntity.ok(new Response("success", "Lấy hóa đơn thành công", InvoiceResponse.from(invoices)));
        } catch (Exception e) {
            log.error("Error getting invoice by code: {}", invoiceCode, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Lấy danh sách hóa đơn của member", description = "Lấy tất cả hóa đơn của một member")
    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MEMBER')")
    public ResponseEntity<Response> getInvoicesByMember(
            @Parameter(description = "ID của member") @PathVariable Long memberId) {
        try {
            List<Invoice> invoices = invoiceService.getInvoicesByMember(memberId);
            return ResponseEntity
                    .ok(new Response("success", "Lấy danh sách hóa đơn thành công", InvoiceResponse.from(invoices)));
        } catch (Exception e) {
            log.error("Error getting invoices for member: {}", memberId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Lấy hóa đơn chưa thanh toán của member", description = "Lấy hóa đơn UNPAID của một member")
    @GetMapping("/member/{memberId}/unpaid")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MEMBER', 'USER')")
    public ResponseEntity<Response> getUnpaidInvoiceByMember(
            @Parameter(description = "ID của member") @PathVariable Long memberId) {
        try {
            List<Invoice> invoices = invoiceService.getUnpaidInvoiceByMember(memberId);
            return ResponseEntity.ok(new Response("success", "Lấy hóa đơn thành công", InvoiceResponse.from(invoices)));
        } catch (Exception e) {
            log.error("Error getting unpaid invoice for member: {}", memberId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Lấy hóa đơn của user", description = "Lấy hóa đơn của một user")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MEMBER', 'USER')")
    public ResponseEntity<Response> getInvoiceByUser(
            @Parameter(description = "ID của user") @PathVariable Long userId) {
        try {
            List<Invoice> invoices = invoiceService.getInvoiceByUser(userId);
            return ResponseEntity.ok(new Response("success", "Lấy hóa đơn thành công", InvoiceResponse.from(invoices)));
        } catch (Exception e) {
            log.error("Error getting invoice for user: {}", userId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    // ============ ADMIN/OWNER ENDPOINTS ============

    @Operation(summary = "Lấy tất cả hóa đơn", description = "Admin lấy danh sách tất cả hóa đơn trong hệ thống")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> getAllInvoices() {
        try {
            List<Invoice> invoices = invoiceService.getAllInvoices();
            return ResponseEntity
                    .ok(new Response("success", "Lấy danh sách hóa đơn thành công", InvoiceResponse.from(invoices)));
        } catch (Exception e) {
            log.error("Error getting all invoices", e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Lấy hóa đơn theo trạng thái", description = "Lọc danh sách hóa đơn theo trạng thái")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> getInvoicesByStatus(
            @Parameter(description = "Trạng thái hóa đơn (UNPAID, PAID, CANCELLED, OVERDUE)") @PathVariable String status) {
        try {
            InvoiceStatus invoiceStatus = InvoiceStatus.valueOf(status.toUpperCase());
            List<Invoice> invoices = invoiceService.getInvoicesByStatus(invoiceStatus);
            return ResponseEntity
                    .ok(new Response("success", "Lấy danh sách hóa đơn thành công", InvoiceResponse.from(invoices)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new Response("error",
                    "Trạng thái không hợp lệ. Chấp nhận: UNPAID, PAID, CANCELLED, OVERDUE", null));
        } catch (Exception e) {
            log.error("Error getting invoices by status: {}", status, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Lấy hóa đơn theo loại", description = "Lọc danh sách hóa đơn theo loại (MEMBERSHIP, PARKING_FEE)")
    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> getInvoicesByType(
            @Parameter(description = "Loại hóa đơn (MEMBERSHIP, PARKING_FEE)") @PathVariable String type) {
        try {
            InvoiceType invoiceType = InvoiceType.valueOf(type.toUpperCase());
            List<Invoice> invoices = invoiceService.getInvoicesByType(invoiceType);
            return ResponseEntity
                    .ok(new Response("success", "Lấy danh sách hóa đơn thành công", InvoiceResponse.from(invoices)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new Response("error",
                    "Loại hóa đơn không hợp lệ. Chấp nhận: MEMBERSHIP, PARKING_FEE", null));
        } catch (Exception e) {
            log.error("Error getting invoices by type: {}", type, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Tạo hóa đơn membership cho member", description = "Admin tạo hóa đơn membership mới cho member")
    @PostMapping("/membership/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> createMembershipInvoice(
            @Parameter(description = "ID của member") @PathVariable Long memberId) {
        try {
            Invoice invoice = invoiceService.createMembershipInvoice(memberId);
            return ResponseEntity.ok(new Response("success", "Tạo hóa đơn thành công", InvoiceResponse.from(invoice)));
        } catch (Exception e) {
            log.error("Error creating membership invoice for member: {}", memberId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Tạo hóa đơn parking fee cho phiên đỗ xe", description = "Tạo hóa đơn phí đỗ xe cho một phiên")
    @PostMapping("/parking-fee/{sessionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'STAFF')")
    public ResponseEntity<Response> createParkingFeeInvoice(
            @Parameter(description = "ID của phiên đỗ xe") @PathVariable Long sessionId) {
        try {
            Invoice invoice = invoiceService.createParkingFeeInvoice(sessionId);
            return ResponseEntity.ok(new Response("success", "Tạo hóa đơn thành công", InvoiceResponse.from(invoice)));
        } catch (Exception e) {
            log.error("Error creating parking fee invoice for session: {}", sessionId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Đánh dấu hóa đơn đã thanh toán", description = "Cập nhật trạng thái hóa đơn thành PAID")
    @PutMapping("/{invoiceId}/mark-paid")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> markInvoiceAsPaid(
            @Parameter(description = "ID của hóa đơn") @PathVariable Long invoiceId) {
        try {
            Invoice invoice = invoiceService.markAsPaid(invoiceId);
            return ResponseEntity
                    .ok(new Response("success", "Đã đánh dấu hóa đơn là đã thanh toán", InvoiceResponse.from(invoice)));
        } catch (Exception e) {
            log.error("Error marking invoice as paid: {}", invoiceId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Đánh dấu hóa đơn quá hạn", description = "Cập nhật trạng thái hóa đơn thành OVERDUE")
    @PutMapping("/{invoiceId}/mark-overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> markInvoiceAsOverdue(
            @Parameter(description = "ID của hóa đơn") @PathVariable Long invoiceId) {
        try {
            Invoice invoice = invoiceService.markAsOverdue(invoiceId);
            return ResponseEntity
                    .ok(new Response("success", "Đã đánh dấu hóa đơn là quá hạn", InvoiceResponse.from(invoice)));
        } catch (Exception e) {
            log.error("Error marking invoice as overdue: {}", invoiceId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Hủy hóa đơn", description = "Cập nhật trạng thái hóa đơn thành CANCELLED")
    @PutMapping("/{invoiceId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> cancelInvoice(
            @Parameter(description = "ID của hóa đơn") @PathVariable Long invoiceId) {
        try {
            Invoice invoice = invoiceService.cancelInvoice(invoiceId);
            return ResponseEntity.ok(new Response("success", "Đã hủy hóa đơn", InvoiceResponse.from(invoice)));
        } catch (Exception e) {
            log.error("Error cancelling invoice: {}", invoiceId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Lấy danh sách hóa đơn quá hạn", description = "Lấy các hóa đơn chưa thanh toán và đã quá deadline")
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> getOverdueInvoices() {
        try {
            List<Invoice> invoices = invoiceService.getOverdueInvoices();
            List<InvoiceResponse> invoiceResponses = InvoiceResponse.from(invoices);
            return ResponseEntity.ok(new Response("success", "Lấy danh sách hóa đơn quá hạn thành công",
                    Map.of(
                            "count", invoiceResponses.size(),
                            "invoices", invoiceResponses)));
        } catch (Exception e) {
            log.error("Error getting overdue invoices", e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Thống kê hóa đơn", description = "Lấy thống kê tổng quan về hóa đơn")
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> getInvoiceStatistics() {
        try {
            Map<String, Object> stats = invoiceService.getInvoiceStatistics();
            return ResponseEntity.ok(new Response("success", "Lấy thống kê hóa đơn thành công", stats));
        } catch (Exception e) {
            log.error("Error getting invoice statistics", e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }
}
