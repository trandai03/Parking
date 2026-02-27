package com.project.parking.repository;

import com.project.parking.model.ParkingSession;
import com.project.parking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository

public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long> {
    @Query("SELECT ps FROM ParkingSession ps WHERE ps.status = :status ORDER BY ps.entryTime DESC")
    List<ParkingSession> findByStatus(@Param("status") String status);

    @Query("SELECT ps FROM ParkingSession ps WHERE ps.status = 'ACTIVE' AND ps.vehicleId = :vehicleId")
    Optional<ParkingSession> findActiveSessionByVehicleId(@Param("vehicleId") Long vehicleId);

    @Query("SELECT ps FROM ParkingSession ps join Vehicle v on ps.vehicleId = v.id WHERE v.licensePlate LIKE %:licensePlate% ORDER BY ps.entryTime DESC")
    List<ParkingSession> findByLicensePlate(@Param("licensePlate") String licensePlate);

    @Query("SELECT ps FROM ParkingSession ps WHERE ps.lot.id = :lotId ORDER BY ps.entryTime DESC")
    List<ParkingSession> findByLotId(@Param("lotId") Long lotId);

    @Query("SELECT ps FROM ParkingSession ps WHERE ps.lot.id = :lotId AND ps.exitTime BETWEEN :startDateTime AND :endDateTime AND ps.status = 'COMPLETED'" )
    List<ParkingSession> findByLotIdAndExitTimeBetween(Long lotId, LocalDateTime startDateTime,
            LocalDateTime endDateTime);

    @Query("SELECT ps FROM ParkingSession ps WHERE ps.code = :code AND ps.status = 'ACTIVE'" )
    Optional<ParkingSession> findByCode(@Param("code") Integer code);

    @Query("SELECT ps FROM ParkingSession ps WHERE ps.user.id = :userId AND ps.status = 'ACTIVE'")
    Optional<ParkingSession> findByUserId(@Param("userId") Long userId);

    @Query("SELECT ps FROM ParkingSession ps WHERE ps.entryTime BETWEEN :dateStart AND :dateEnd OR ps.exitTime BETWEEN :dateStart AND :dateEnd ORDER BY ps.entryTime DESC")
    List<ParkingSession> findByDateTime(@Param("dateStart") LocalDateTime dateStart,
            @Param("dateEnd") LocalDateTime dateEnd);

//     @Query("SELECT ps FROM ParkingSession ps WHERE ps.lot.id = :lotId AND ps.entryTime BETWEEN :dateStart AND :dateEnd OR ps.exitTime BETWEEN :dateStart AND :dateEnd AND ps.vehicle.licensePlate LIKE %:licensePlate%")
//     List<ParkingSession> findAll(@Param("lotId") Long lotId, @Param("dateStart") LocalDateTime dateStart,
//             @Param("dateEnd") LocalDateTime dateEnd, @Param("licensePlate") String licensePlate);
    @Override
    @Query("SELECT ps FROM ParkingSession ps order by ps.entryTime DESC")
    List<ParkingSession> findAll();

    @Query("SELECT ps FROM ParkingSession ps WHERE ps.memberId = :memberId AND ps.vehicleId = :vehicleId AND ps.status = 'ACTIVE'" )
    Optional<ParkingSession> findByMemberIdAndVehicle(@Param("memberId") Long memberId,@Param("vehicleId") Long vehicleId);

    
}