package com.project.parking.repository;

import com.project.parking.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findByStatus(String status);

    Optional<Shift> findByShiftName(String shiftName);

    List<Shift> findByStartTimeGreaterThanEqualAndEndTimeLessThanEqual(LocalTime startTime, LocalTime endTime);
    @Query("SELECT s FROM Shift s WHERE s.endTime <= :currentTime " +
            "OR (s.endTime = :midnight AND :currentTime >= :lateNight)")
    List<Shift> findCompletedShiftsForTime(@Param("currentTime") LocalTime currentTime,
                                           @Param("midnight") LocalTime midnight,
                                           @Param("lateNight") LocalTime lateNight);

}