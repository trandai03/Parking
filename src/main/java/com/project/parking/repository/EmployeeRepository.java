package com.project.parking.repository;

import com.project.parking.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByParkingLotId(Long parkingLotId);

    List<Employee> findByStatus(String status);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Employee e WHERE e.user.id = :userId AND e.parkingLot.id = :parkingLotId")
    boolean existsByUserIdAndParkingLotId(@Param("userId") Long userId, @Param("parkingLotId") Long parkingLotId);


    Optional<Employee> findByUserId(Long userId);

    @Query("SELECT e FROM Employee e join User u on e.user.id = u.id WHERE u.fullname LIKE %:name%")
    List<Employee> findByName(@Param("name") String name);
}