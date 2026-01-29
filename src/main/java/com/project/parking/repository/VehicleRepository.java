package com.project.parking.repository;

import com.project.parking.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    boolean existsByLicensePlate(String licensePlate);

    List<Vehicle> findByMemberId(Long memberId);

    Optional<Vehicle> findByMemberIdAndLicensePlate(Long memberId, String licensePlate);
}