package com.project.parking.repository;

import com.project.parking.model.ParkingPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingPlanRepository extends JpaRepository<ParkingPlan, Long> {

    /**
     * Find all plans by parking lot
     */
    List<ParkingPlan> findByParkingLotId(Long parkingLotId);

    /**
     * Find active plans by parking lot
     */
    List<ParkingPlan> findByParkingLotIdAndIsActiveTrue(Long parkingLotId);

    /**
     * Find plans by parking lot ordered by sort order
     */
    @Query("SELECT p FROM ParkingPlan p WHERE p.parkingLot.id = :parkingLotId " +
           "AND p.isActive = true ORDER BY p.sortOrder ASC")
    List<ParkingPlan> findActivePlansByParkingLotOrdered(@Param("parkingLotId") Long parkingLotId);

    /**
     * Find plans by type
     */
    List<ParkingPlan> findByPlanType(String planType);

    /**
     * Find popular plans
     */
    List<ParkingPlan> findByIsPopularTrueAndIsActiveTrue();

    /**
     * Find plans by price unit
     */
    List<ParkingPlan> findByPriceUnit(String priceUnit);

    /**
     * Check if plan name exists for parking lot
     */
    boolean existsByParkingLotIdAndName(Long parkingLotId, String name);

    /**
     * Count plans by parking lot
     */
    Long countByParkingLotId(Long parkingLotId);

    /**
     * Count active plans by parking lot
     */
    Long countByParkingLotIdAndIsActiveTrue(Long parkingLotId);
}

