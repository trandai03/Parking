package com.project.parking.service;

import com.project.parking.enums.InvoiceStatus;
import com.project.parking.enums.InvoiceType;
import com.project.parking.exceptions.DataNotFoundException;
import com.project.parking.model.Invoice;
import com.project.parking.model.Member;
import com.project.parking.model.ParkingSession;
import com.project.parking.repository.InvoiceRepository;
import com.project.parking.repository.MemberRepository;
import com.project.parking.repository.ParkingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service quản lý Invoice (hóa đơn)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final MemberRepository memberRepository;
    private final ParkingSessionRepository parkingSessionRepository;

    // Counter cho invoice code (trong production nên dùng sequence từ DB)
    private static final AtomicLong invoiceCounter = new AtomicLong(1);

    /**
     * Tạo hóa đơn phí thành viên
     */
    @Transactional
    public Invoice createMembershipInvoice(Long memberId) throws DataNotFoundException {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new DataNotFoundException("Member không tồn tại với ID: " + memberId));

        // Kiểm tra đã có invoice UNPAID chưa
        // invoiceRepository.findFirstByMemberIdAndStatusOrderByCreatedAtDesc(memberId, InvoiceStatus.UNPAID)
        //         .ifPresent(existing -> {
        //             throw new IllegalStateException("Đã có hóa đơn chưa thanh toán cho member này");
        //         });

        BigDecimal amount = member.getMembershipFee();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Phí thành viên không hợp lệ");
        }

        Invoice invoice = Invoice.builder()
                .memberId(memberId)
                .parkingPlanId(member.getParkingPlan() != null ? member.getParkingPlan().getId() : null)
                .type(InvoiceType.MEMBERSHIP)
                .invoiceCode(generateInvoiceCode("MEM"))
                .amount(amount)
                .description("Phí thành viên - " + member.getMemberCode())
                .status(InvoiceStatus.UNPAID)
                .paymentDeadline(LocalDateTime.now().plusDays(5))
                .build();

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Created membership invoice for member {}: invoiceCode={}, amount={}",
                memberId, saved.getInvoiceCode(), amount);

        return saved;
    }

    /**
     * Tạo hóa đơn phí đỗ xe lượt
     */
    @Transactional
    public Invoice createParkingFeeInvoice(Long sessionId, BigDecimal amount) throws DataNotFoundException {
        ParkingSession session = parkingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new DataNotFoundException("Phiên đỗ xe không tồn tại với ID: " + sessionId));

        Invoice invoice = Invoice.builder()
                .sessionId(sessionId)
                .type(InvoiceType.PARKING_FEE)
                .invoiceCode(generateInvoiceCode("PKG"))
                .amount(amount)
                .description("Phí đỗ xe - Session #" + sessionId)
                .status(InvoiceStatus.UNPAID)
                .build();

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Created parking fee invoice for session {}: invoiceCode={}, amount={}",
                sessionId, saved.getInvoiceCode(), amount);

        return saved;
    }

    /**
     * Đánh dấu hóa đơn đã thanh toán
     */
    @Transactional
    public Invoice markAsPaid(Long invoiceId) throws DataNotFoundException {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new DataNotFoundException("Hóa đơn không tồn tại với ID: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            log.warn("Invoice {} already paid", invoiceId);
            return invoice;
        }

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice {} marked as PAID", invoiceId);

        return saved;
    }

    /**
     * Đánh dấu hóa đơn quá hạn
     */
    @Transactional
    public Invoice markAsOverdue(Long invoiceId) throws DataNotFoundException {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new DataNotFoundException("Hóa đơn không tồn tại với ID: " + invoiceId));

        if (invoice.getStatus() != InvoiceStatus.UNPAID) {
            log.warn("Cannot mark invoice {} as overdue, current status: {}", invoiceId, invoice.getStatus());
            return invoice;
        }

        invoice.setStatus(InvoiceStatus.OVERDUE);
        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice {} marked as OVERDUE", invoiceId);

        return saved;
    }

    /**
     * Hủy hóa đơn
     */
    @Transactional
    public Invoice cancelInvoice(Long invoiceId) throws DataNotFoundException {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new DataNotFoundException("Hóa đơn không tồn tại với ID: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("Không thể hủy hóa đơn đã thanh toán");
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice {} cancelled", invoiceId);

        return saved;
    }

    /**
     * Lấy hóa đơn theo ID
     */
    public Invoice getInvoiceById(Long invoiceId) throws DataNotFoundException {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new DataNotFoundException("Hóa đơn không tồn tại với ID: " + invoiceId));
    }

    /**
     * Lấy hóa đơn UNPAID của member
     */
    public List<Invoice> getUnpaidInvoiceByMember(Long memberId) throws DataNotFoundException {
        return invoiceRepository.findByMemberIdAndStatusOrderByCreatedAtDesc(memberId, InvoiceStatus.UNPAID);
    }

    /**
     * Lấy danh sách hóa đơn của member
     */
    public List<Invoice> getInvoicesByMember(Long memberId) {
        return invoiceRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    /**
     * Lấy hóa đơn của user (dùng cho cả MEMBER và USER)
     */
    public List<Invoice> getInvoiceByUser(Long userId) throws DataNotFoundException {
        // Tìm hóa đơn  của user
        return invoiceRepository.findByUserId(userId);
    }

    /**
     * Lấy danh sách hóa đơn quá hạn
     */
    public List<Invoice> getOverdueInvoices() {
        return invoiceRepository.findOverdueInvoices(InvoiceStatus.UNPAID, LocalDateTime.now());
    }

    /**
     * Lấy danh sách hóa đơn sắp hết hạn
     */
    public List<Invoice> getInvoicesNearDeadline(int daysAhead) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusDays(daysAhead);
        return invoiceRepository.findInvoicesNearDeadline(InvoiceStatus.UNPAID, now, deadline);
    }

    /**
     * Lấy tất cả hóa đơn
     */
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    /**
     * Lấy hóa đơn theo mã
     */
    public List<Invoice> getInvoiceByCode(String invoiceCode) throws DataNotFoundException {
        List<Invoice> invoices = invoiceRepository.findByInvoiceCode(invoiceCode);
        if (invoices.isEmpty()) {
            throw new DataNotFoundException("Hóa đơn không tồn tại với mã: " + invoiceCode);
        }
        return invoices;
    }

    /**
     * Lấy danh sách hóa đơn theo trạng thái
     */
    public List<Invoice> getInvoicesByStatus(InvoiceStatus status) {
        return invoiceRepository.findByStatus(status);
    }

    /**
     * Lấy danh sách hóa đơn theo loại
     */
    public List<Invoice> getInvoicesByType(InvoiceType type) {
        return invoiceRepository.findByType(type);
    }

    /**
     * Tạo hóa đơn phí đỗ xe (overload không cần amount)
     */
    @Transactional
    public Invoice createParkingFeeInvoice(Long sessionId) throws DataNotFoundException {
        ParkingSession session = parkingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new DataNotFoundException("Phiên đỗ xe không tồn tại với ID: " + sessionId));

        BigDecimal amount = session.getTotalCost() != null ? session.getTotalCost() : BigDecimal.ZERO;

        Invoice invoice = Invoice.builder()
                .sessionId(sessionId)
                .type(InvoiceType.PARKING_FEE)
                .invoiceCode(generateInvoiceCode("PKG"))
                .amount(amount)
                .description("Phí đỗ xe - Session #" + sessionId)
                .status(InvoiceStatus.UNPAID)
                .build();

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Created parking fee invoice for session {}: invoiceCode={}, amount={}",
                sessionId, saved.getInvoiceCode(), amount);

        return saved;
    }

    /**
     * Lấy thống kê hóa đơn
     */
    public java.util.Map<String, Object> getInvoiceStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        stats.put("totalInvoices", invoiceRepository.count());
        stats.put("unpaidCount", invoiceRepository.countByStatus(InvoiceStatus.UNPAID));
        stats.put("paidCount", invoiceRepository.countByStatus(InvoiceStatus.PAID));
        stats.put("overdueCount", invoiceRepository.countByStatus(InvoiceStatus.OVERDUE));
        stats.put("cancelledCount", invoiceRepository.countByStatus(InvoiceStatus.CANCELLED));

        stats.put("membershipInvoices", invoiceRepository.countByType(InvoiceType.MEMBERSHIP));
        stats.put("parkingFeeInvoices", invoiceRepository.countByType(InvoiceType.PARKING_FEE));

        // Tổng doanh thu từ hóa đơn đã thanh toán
        BigDecimal totalPaidAmount = invoiceRepository.sumAmountByStatus(InvoiceStatus.PAID);
        stats.put("totalPaidAmount", totalPaidAmount != null ? totalPaidAmount : BigDecimal.ZERO);

        // Tổng số tiền chưa thu
        BigDecimal totalUnpaidAmount = invoiceRepository.sumAmountByStatus(InvoiceStatus.UNPAID);
        stats.put("totalUnpaidAmount", totalUnpaidAmount != null ? totalUnpaidAmount : BigDecimal.ZERO);

        return stats;
    }

    /**
     * Generate mã hóa đơn unique
     */
    private String generateInvoiceCode(String prefix) {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long counter = invoiceCounter.getAndIncrement();
        return String.format("%s-%s-%04d", prefix, datePart, counter);
    }
}
