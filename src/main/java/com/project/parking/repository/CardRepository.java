package com.project.parking.repository;

import com.project.parking.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByCardNumber(String cardNumber);

    List<Card> findByUserId(Long userId);

    List<Card> findByVehicleId(Long vehicleId);

    List<Card> findByStatus(String status);

    @Query("SELECT c FROM Card c JOIN c.vehicle v WHERE v.licensePlate = :licensePlate")
    List<Card> findByVehicleLicensePlate(@Param("licensePlate") String licensePlate);
}