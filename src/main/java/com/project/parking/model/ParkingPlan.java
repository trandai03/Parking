package com.project.parking.model;

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
@Table(name = "parking_plans")
public class ParkingPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "parking_lot_id", nullable = false)
    private ParkingLot parkingLot;

    @Size(max = 100)
    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull
    @Size(max = 20)
    @Column(name = "price_unit", nullable = false, length = 20)
    private String priceUnit; // HOUR, DAY, MONTH, QUARTER, YEAR

    @NotNull
    @Size(max = 20)
    @Column(name = "plan_type", nullable = false, length = 20)
    private String planType; // PREMIUM, STANDARD, BASIC, ECONOMY

    @Column(name = "is_unlimited_parking")
    private Boolean isUnlimitedParking = false;

    @Column(name = "has_fixed_spot")
    private Boolean hasFixedSpot = false;

    @Column(name = "has_valet_service")
    private Boolean hasValetService = false;

    @Column(name = "has_car_wash")
    private Boolean hasCarWash = false;

    @Column(name = "has_covered_parking")
    private Boolean hasCoveredParking = false;

    @Column(name = "has_security_24_7")
    private Boolean hasSecurity247 = false;

    @Column(name = "is_popular")
    private Boolean isPopular = false;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "duration_months")
    private Integer durationMonths = 1; // Số tháng membership, mặc định 1 tháng

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
