package com.project.parking.repository;

import com.project.parking.model.EmployeeShift;
import com.project.parking.model.ShiftAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftAttendanceRepository extends JpaRepository<ShiftAttendance, Long> {
    Optional<ShiftAttendance> findByEmployeeShiftId(Long employeeShiftId);

    Optional<ShiftAttendance> findByEmployeeShift(EmployeeShift employeeShift);

    List<ShiftAttendance> findByStatus(String status);

    @Query("SELECT sa FROM ShiftAttendance sa WHERE sa.employeeShift.employee.id = :employeeId AND sa.checkInTime BETWEEN :startDate AND :endDate")
    List<ShiftAttendance> findByEmployeeIdAndDateRange(Long employeeId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT sa FROM ShiftAttendance sa WHERE sa.employeeShift.parkingLot.id = :parkingLotId AND sa.checkInTime BETWEEN :startDate AND :endDate")
    List<ShiftAttendance> findByParkingLotIdAndDateRange(Long parkingLotId, LocalDateTime startDate,
            LocalDateTime endDate);
}