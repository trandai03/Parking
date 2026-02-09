package com.project.parking.repository;

import com.project.parking.enums.PaymentStatus;
import com.project.parking.model.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

        /**
         * Tìm lịch sử thanh toán theo invoice
         */
        List<PaymentHistory> findByInvoiceIdOrderByCreatedAtDesc(Long invoiceId);

        /**
         * Tìm lịch sử thanh toán theo invoice và trạng thái
         */
        List<PaymentHistory> findByInvoiceIdAndPaymentStatus(Long invoiceId, PaymentStatus paymentStatus);

        /**
         * Tìm thanh toán pending của invoice
         */
        Optional<PaymentHistory> findFirstByInvoiceIdAndPaymentStatusOrderByCreatedAtDesc(
                        Long invoiceId, PaymentStatus paymentStatus);

        /**
         * Tìm theo order ID (MoMo/VNPAY)
         */
        Optional<PaymentHistory> findByOrderId(String orderId);

        /**
         * JOIN với Invoice để lấy lịch sử thanh toán của member
         */
        @Query("SELECT ph, i FROM PaymentHistory ph JOIN Invoice i ON ph.invoiceId = i.id " +
                        "WHERE i.memberId = :memberId ORDER BY ph.createdAt DESC")
        List<Object[]> findPaymentHistoryByMemberId(@Param("memberId") Long memberId);

        /**
         * JOIN với Invoice để lấy lịch sử thanh toán của session
         */
        @Query("SELECT ph, i FROM PaymentHistory ph JOIN Invoice i ON ph.invoiceId = i.id " +
                        "WHERE i.sessionId = :sessionId ORDER BY ph.createdAt DESC")
        List<Object[]> findPaymentHistoryBySessionId(@Param("sessionId") Long sessionId);

        /**
         * Đếm số thanh toán theo trạng thái
         */
        Long countByPaymentStatus(PaymentStatus paymentStatus);

        /**
         * Đếm số thanh toán thành công của invoice
         */
        Long countByInvoiceIdAndPaymentStatus(Long invoiceId, PaymentStatus paymentStatus);

        /**
         * Tìm tất cả payment history với invoice chi tiết
         */
        @Query("SELECT ph, i FROM PaymentHistory ph JOIN Invoice i ON ph.invoiceId = i.id " +
                        "WHERE ph.paymentStatus = :status ORDER BY ph.createdAt DESC")
        List<Object[]> findAllWithInvoiceByStatus(@Param("status") PaymentStatus status);
}
