package com.project.parking.repository;

import com.project.parking.model.RevenueStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RevenueStatRepository extends JpaRepository<RevenueStat, Long> {
    List<RevenueStat> findByLotId(Long lotId);

    default List<RevenueStat> findByParkingLotId(Long parkingLotId) {
        return findByLotId(parkingLotId);
    }

    List<RevenueStat> findByStatDateBetween(LocalDate startDate, LocalDate endDate);

    default List<RevenueStat> findByDateBetween(LocalDate startDate, LocalDate endDate) {
        return findByStatDateBetween(startDate, endDate);
    }

    List<RevenueStat> findByLotIdAndStatDateBetween(Long lotId, LocalDate startDate, LocalDate endDate);

    default List<RevenueStat> findByParkingLotIdAndDateBetween(Long parkingLotId, LocalDate startDate,
            LocalDate endDate) {
        return findByLotIdAndStatDateBetween(parkingLotId, startDate, endDate);
    }

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM RevenueStat r WHERE r.lot.id = :lotId AND r.statDate = :date")
    boolean existsByLotIdAndStatDate(@Param("lotId") Long lotId, @Param("date") LocalDate date);

    default boolean existsByParkingLotIdAndDate(Long parkingLotId, LocalDate date) {
        return existsByLotIdAndStatDate(parkingLotId, date);
    }

    @Query("SELECT r FROM RevenueStat r WHERE (:lotId IS NULL OR r.lot.id = :lotId) AND (:startDate IS NULL OR r.statDate BETWEEN :startDate AND :endDate)")
    List<RevenueStat> findAll(@Param("lotId") Long lotId, @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}