package com.project.parking.repository;

import com.project.parking.enums.PaymentStatus;
import com.project.parking.model.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    /**
     * Tìm lịch sử thanh toán theo member
     */
    List<PaymentHistory> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    /**
     * Tìm lịch sử thanh toán theo member và trạng thái
     */
    List<PaymentHistory> findByMemberIdAndPaymentStatus(Long memberId, PaymentStatus paymentStatus);

    /**
     * Tìm thanh toán pending của member
     */
    Optional<PaymentHistory> findFirstByMemberIdAndPaymentStatusOrderByCreatedAtDesc(Long memberId, PaymentStatus paymentStatus);

    /**
     * Tìm theo order ID
     */
    Optional<PaymentHistory> findByOrderId(String orderId);

    /**
     * Tìm các thanh toán quá hạn (pending và đã qua deadline)
     */
    @Query("SELECT ph FROM PaymentHistory ph WHERE ph.paymentStatus = :status " +
           "AND ph.paymentDeadline < :now")
    List<PaymentHistory> findOverduePayments(
            @Param("status") PaymentStatus status,
            @Param("now") LocalDateTime now
    );

    /**
     * Tìm các thanh toán sắp hết hạn (trong vòng X ngày)
     */
    @Query("SELECT ph FROM PaymentHistory ph WHERE ph.paymentStatus = :status " +
           "AND ph.paymentDeadline BETWEEN :now AND :deadline")
    List<PaymentHistory> findPaymentsNearDeadline(
            @Param("status") PaymentStatus status,
            @Param("now") LocalDateTime now,
            @Param("deadline") LocalDateTime deadline
    );

    /**
     * Đếm số thanh toán theo trạng thái
     */
    Long countByPaymentStatus(PaymentStatus paymentStatus);

    /**
     * Đếm số thanh toán thành công của member
     */
    Long countByMemberIdAndPaymentStatus(Long memberId, PaymentStatus paymentStatus);
}

