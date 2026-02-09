package com.project.parking.model;

import com.project.parking.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity lưu lịch sử các lần thanh toán thực tế
 * Mỗi PaymentHistory gắn với 1 Invoice
 * Dùng ID-based relationship (invoiceId) thay vì @ManyToOne
 */
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payment_history")
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_history_id", nullable = false)
    private Long id;

    // ID-based relationship với Invoice
    @NotNull
    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Size(max = 50)
    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // MOMO, VNPAY, CASH, BANK_TRANSFER

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Size(max = 100)
    @Column(name = "order_id", length = 100)
    private String orderId; // MoMo/VNPAY order ID

    @Size(max = 500)
    @Column(name = "payment_url", length = 500)
    private String paymentUrl; // Link thanh toán online

    // Nhân viên xử lý (nếu thanh toán tiền mặt)
    @Column(name = "processed_by_user_id")
    private Long processedByUserId;

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
