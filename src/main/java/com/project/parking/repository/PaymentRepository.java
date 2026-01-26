package com.project.parking.repository;

import com.project.parking.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p WHERE p.session.id = :sessionId")
    List<Payment> findBySessionId(Long sessionId);

    @Query("SELECT p FROM Payment p WHERE p.session.id = :sessionId AND p.paymentStatus = :status")
    List<Payment> findBySessionIdAndStatus(@Param("sessionId") Long sessionId, @Param("status") String status);

    List<Payment> findByUserId(Long userId);
}