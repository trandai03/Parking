package com.project.parking.model;

import com.project.parking.enums.MemberStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "members")
public class Member {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", nullable = false)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id")
    private ParkingLot parkingLot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private ParkingPlan parkingPlan;

    @Size(max = 50)
    @Column(name = "member_code", unique = true, length = 50)
    private String memberCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_status")
    private MemberStatus memberStatus;

    @Column(name = "membership_start_date")
    private LocalDateTime membershipStartDate;

    @Column(name = "membership_expiry_date")
    private LocalDateTime membershipExpiryDate;

    @Column(name = "membership_accept_date")
    private LocalDateTime membershipAcceptDate;

    @Column(name = "membership_fee", precision = 10, scale = 2)
    private BigDecimal membershipFee;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Size(max = 500)
    @Column(name = "lock_reason", length = 500)
    private String lockReason;

    @Size(max = 50)
    @Column(name = "room_number", unique = true, length = 50)
    private String roomNumber;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY,orphanRemoval = true)
    private List<Vehicle> vehicles;

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

