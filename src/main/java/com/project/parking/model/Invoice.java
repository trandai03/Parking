package com.project.parking.model;

import com.project.parking.enums.InvoiceStatus;
import com.project.parking.enums.InvoiceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity lưu thông tin hóa đơn
 * Dùng ID-based relationships thay vì @ManyToOne để tránh khóa ngoài null
 */
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id", nullable = false)
    private Long id;

    // ID-based relationships (không dùng @ManyToOne)
    @Column(name = "member_id")
    private Long memberId; // Nếu type = MEMBERSHIP

    @Column(name = "session_id")
    private Long sessionId; // Nếu type = PARKING_FEE

    @Column(name = "parking_plan_id")
    private Long parkingPlanId; // Nếu type = MEMBERSHIP

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_type", nullable = false)
    private InvoiceType type;

    @NotNull
    @Size(max = 50)
    @Column(name = "invoice_code", nullable = false, unique = true, length = 50)
    private String invoiceCode; // INV-20260203-001

    @NotNull
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_status", nullable = false)
    private InvoiceStatus status = InvoiceStatus.UNPAID;

    @Column(name = "payment_deadline")
    private LocalDateTime paymentDeadline;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
