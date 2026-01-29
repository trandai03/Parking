package com.project.parking.service;

import com.project.parking.dto.EmployeeDTO;
import com.project.parking.dto.request.EmployeeRequest;
import com.project.parking.dto.request.UpdatePasswordDTO;
import com.project.parking.dto.request.UserDTO;
import com.project.parking.enums.Role;
import com.project.parking.exceptions.DataNotFoundException;
import com.project.parking.model.Employee;
import com.project.parking.model.ParkingLot;
import com.project.parking.model.User;
import com.project.parking.repository.EmployeeRepository;
import com.project.parking.repository.ParkingLotRepository;
import com.project.parking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public List<EmployeeDTO> getAllEmployees(String name) {
        if(name != null){
            return EmployeeDTO.fromEmployees(employeeRepository.findByName(name));
        }
        return employeeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EmployeeDTO getEmployeeById(Long id) throws DataNotFoundException {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Employee not found with id: " + id));
        return convertToDTO(employee);
    }

    public List<EmployeeDTO> getEmployeesByParkingLot(Long parkingLotId) {
        return employeeRepository.findByParkingLotId(parkingLotId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EmployeeDTO> getEmployeesByStatus(String status) {
        return employeeRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EmployeeDTO getEmployeeByUserId(Long userId) throws DataNotFoundException {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new DataNotFoundException("Employee not found with user id: " + userId));
        return convertToDTO(employee);
    }

    @Transactional
    public Employee createEmployee(EmployeeRequest employeeRequest) throws Exception {

        UserDTO userDTO = employeeRequest.getUserDTO();
        if (userDTO == null) {
            throw new DataNotFoundException("User information is required");
        }
        User user = User.builder()
                    .email(userDTO.getEmail())
                    .username(userDTO.getUsername())
                    .password(passwordEncoder.encode(userDTO.getPassword()))
                    .verificationCode(null)
                    .active(true)
                    .fullname(userDTO.getFullname())
                    .dateOfBirth(userDTO.getDateOfBirth())
                    .phoneNumber(userDTO.getPhoneNumber())
                    .verificationExpiration(null)
                    .role(Role.EMPLOYEE)
                    .build();
        userRepository.save(user);

        Employee employee = new Employee();
        ParkingLot parkingLot = null;
        if (employeeRequest.getParkingLotId() != null) {
            parkingLot = parkingLotRepository.findById(employeeRequest.getParkingLotId())
                    .orElseThrow(() -> new DataNotFoundException(
                            "Parking lot not found with id: " + employeeRequest.getParkingLotId()));
            employee.setParkingLot(parkingLot);
        }
        employee.setUser(user);

        if (employeeRequest.getJoinDate() != null) {
            employee.setJoinDate(employeeRequest.getJoinDate().toLocalDate());
        } else {
            employee.setJoinDate(LocalDate.now());
        }

        employee.setHireDate(LocalDateTime.now());
        employee.setStatus(employeeRequest.getStatus() != null ? employeeRequest.getStatus() : "ACTIVE");
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());

        Employee savedEmployee = employeeRepository.save(employee);
        return savedEmployee;
    }

    public EmployeeDTO updateEmployee(Long id, EmployeeRequest employeeRequest) throws Exception {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Employee not found with id: " + id));

        // if (employeeRequest.getUserDTO().getFullName() != null) {
        //     employee.setName(employeeRequest.getName());
        // }
        if (employeeRequest.getParkingLotId() != null) {
            ParkingLot parkingLot = parkingLotRepository.findById(employeeRequest.getParkingLotId())
                    .orElseThrow(() -> new DataNotFoundException(
                            "Parking lot not found with id: " + employeeRequest.getParkingLotId()));
            employee.setParkingLot(parkingLot);
        }
        if (employeeRequest.getUserDTO() != null) {
            User user = userRepository.findById(employee.getUser().getId())
                    .orElseThrow(() -> new DataNotFoundException("User not found with id: " + employee.getUser().getId()));
            userService.updateInfo(user.getId(), employeeRequest.getUpdateUserDTO());
            employee.setUser(user);

        }
        if (employeeRequest.getJoinDate() != null) {
            employee.setJoinDate(employeeRequest.getJoinDate().toLocalDate());
        }
        if (employeeRequest.getStatus() != null) {
            employee.setStatus(employeeRequest.getStatus());
        }
        employee.setUpdatedAt(LocalDateTime.now());

        Employee updatedEmployee = employeeRepository.save(employee);
        return convertToDTO(updatedEmployee);
    }

    public void deleteEmployee(Long id) throws DataNotFoundException {
        if (!employeeRepository.existsById(id)) {
            throw new DataNotFoundException("Employee not found with id: " + id);
        }
        employeeRepository.deleteById(id);
    }

    private EmployeeDTO convertToDTO(Employee employee) {
        return EmployeeDTO.fromEmployee(employee);
    }
}