package com.project.parking.repository;

import com.project.parking.enums.InvoiceStatus;
import com.project.parking.enums.InvoiceType;
import com.project.parking.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

        /**
         * Tìm hóa đơn theo mã invoice
         */
        @Query("SELECT i FROM Invoice i WHERE i.invoiceCode LIKE %:invoiceCode% ORDER BY i.createdAt DESC")
        List<Invoice> findByInvoiceCode(@Param("invoiceCode") String invoiceCode);

        /**
         * Tìm hóa đơn theo member
         */
        List<Invoice> findByMemberIdOrderByCreatedAtDesc(Long memberId);

        /**
         * Tìm hóa đơn theo session (phí đỗ xe)
         */
        List<Invoice> findBySessionId(Long sessionId);

        /**
         * Tìm hóa đơn theo member và trạng thái
         */
        List<Invoice> findByMemberIdAndStatus(Long memberId, InvoiceStatus status);

        /**
         * Tìm hóa đơn UNPAID của member
         */
        List<Invoice> findByMemberIdAndStatusOrderByCreatedAtDesc(Long memberId, InvoiceStatus status);

        /**
         * Tìm hóa đơn của user
         */
        @Query("SELECT i FROM Invoice i JOIN Member m ON i.memberId = m.id WHERE m.user.id = :userId")
        List<Invoice> findByUserId(@Param("userId") Long userId);

        /**
         * Tìm hóa đơn theo loại và trạng thái
         */
        List<Invoice> findByTypeAndStatus(InvoiceType type, InvoiceStatus status);

        /**
         * Tìm các hóa đơn quá hạn (UNPAID và đã qua deadline)
         */
        @Query("SELECT i FROM Invoice i WHERE i.status = :status AND i.paymentDeadline < :now")
        List<Invoice> findOverdueInvoices(
                        @Param("status") InvoiceStatus status,
                        @Param("now") LocalDateTime now);

        /**
         * Tìm các hóa đơn sắp hết hạn (trong khoảng thời gian)
         */
        @Query("SELECT i FROM Invoice i WHERE i.status = :status " +
                        "AND i.paymentDeadline BETWEEN :start AND :end")
        List<Invoice> findInvoicesNearDeadline(
                        @Param("status") InvoiceStatus status,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        /**
         * JOIN với Member để lấy thông tin chi tiết
         */
        @Query("SELECT i, m FROM Invoice i JOIN Member m ON i.memberId = m.id " +
                        "WHERE i.status = :status ORDER BY i.createdAt DESC")
        List<Object[]> findInvoicesWithMember(@Param("status") InvoiceStatus status);

        /**
         * JOIN với ParkingSession để lấy thông tin chi tiết
         */
        @Query("SELECT i, ps FROM Invoice i JOIN ParkingSession ps ON i.sessionId = ps.id " +
                        "WHERE i.status = :status ORDER BY i.createdAt DESC")
        List<Object[]> findInvoicesWithSession(@Param("status") InvoiceStatus status);

        /**
         * Đếm số hóa đơn theo trạng thái
         */
        Long countByStatus(InvoiceStatus status);

        /**
         * Đếm số hóa đơn của member theo trạng thái
         */
        Long countByMemberIdAndStatus(Long memberId, InvoiceStatus status);

        /**
         * Tìm hóa đơn theo trạng thái
         */
        List<Invoice> findByStatus(InvoiceStatus status);

        /**
         * Tìm hóa đơn theo loại
         */
        List<Invoice> findByType(InvoiceType type);

        /**
         * Đếm số hóa đơn theo loại
         */
        Long countByType(InvoiceType type);

        /**
         * Tính tổng số tiền theo trạng thái
         */
        @Query("SELECT SUM(i.amount) FROM Invoice i WHERE i.status = :status")
        java.math.BigDecimal sumAmountByStatus(@Param("status") InvoiceStatus status);
}
