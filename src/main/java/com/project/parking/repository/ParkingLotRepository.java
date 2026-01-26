package com.project.parking.repository;

import com.project.parking.model.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
    List<ParkingLot> findByStatus(String status);

    List<ParkingLot> findByOwnerId(Long ownerId);

    @Query("SELECT p FROM ParkingLot p WHERE " +
            "(:name IS NULL OR p.name LIKE %:name%) AND " +
            "(:vehicleTypes IS NULL OR p.vehicleTypes IN :vehicleTypes) AND " +
            "(:isCovered IS NULL OR p.isCovered = :isCovered) AND " +
            "(:status IS NULL OR p.status = :status)")
    List<ParkingLot> getAllParkingLot(@Param("name") String name, @Param("vehicleTypes") List<String> vehicleTypes,
                                      @Param("isCovered") Boolean isCovered, @Param("status") String status);
}