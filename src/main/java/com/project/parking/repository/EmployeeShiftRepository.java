package com.project.parking.repository;

import com.project.parking.model.Employee;
import com.project.parking.model.EmployeeShift;
import com.project.parking.model.ParkingLot;
import com.project.parking.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeShiftRepository extends JpaRepository<EmployeeShift, Long> {
    List<EmployeeShift> findByEmployeeId(Long employeeId);

    List<EmployeeShift> findByShiftId(Long shiftId);

    List<EmployeeShift> findByWorkDate(LocalDate workDate);

    List<EmployeeShift> findByEmployeeAndWorkDateBetween(Employee employee, LocalDate startDate, LocalDate endDate);

    List<EmployeeShift> findByParkingLotAndWorkDate(ParkingLot parkingLot, LocalDate workDate);

    List<EmployeeShift> findByParkingLotAndShiftAndWorkDate(ParkingLot parkingLot, Shift shift, LocalDate workDate);

    List<EmployeeShift> findByIsRecurringTrueAndDayOfWeek(DayOfWeek dayOfWeek);

    List<EmployeeShift> findByStatus(String status);

    List<EmployeeShift> findByShiftIdAndWorkDate(Long shiftId, LocalDate workDate);

    @Query("SELECT es FROM EmployeeShift es WHERE " +
            "(:shiftId IS NULL OR es.shift.id = :shiftId) AND " +
            "(:workDate IS NULL OR es.workDate = :workDate) " +
            "ORDER BY es.workDate ASC , es.employee.id ASC")
    List<EmployeeShift> findAll(@Param("shiftId") Long shiftId, @Param("workDate") LocalDate workDate);

    @Query("SELECT es FROM EmployeeShift es WHERE es.employee.id = :employeeId AND es.shift.id = :shiftId AND es.workDate = :workDate")
    EmployeeShift findByEmployeeAndShiftAndWorkDate(@Param("employeeId") Long employeeId,
            @Param("shiftId") Long shiftId, @Param("workDate") LocalDate workDate);
}