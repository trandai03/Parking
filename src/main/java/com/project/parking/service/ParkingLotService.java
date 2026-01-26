package com.project.parking.service;

import com.project.parking.dto.ParkingLotDTO;
import com.project.parking.dto.request.EmployeeRequest;
import com.project.parking.dto.request.ParkingLotRequest;
import com.project.parking.exceptions.DataNotFoundException;
import com.project.parking.model.Employee;
import com.project.parking.model.ParkingLot;
import com.project.parking.model.User;
import com.project.parking.repository.EmployeeRepository;
import com.project.parking.repository.ParkingLotRepository;
import com.project.parking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;

    public List<ParkingLotDTO> getAllParkingLots() {
        return parkingLotRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ParkingLotDTO> getAllParkingLots(String name, List<String> vehicleTypes, Boolean isCovered,
            String status) {
        List<ParkingLot> filteredLots = parkingLotRepository.getAllParkingLot(name, vehicleTypes, isCovered, status);

        return filteredLots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ParkingLotDTO getParkingLotById(Long id) throws DataNotFoundException {
        ParkingLot parkingLot = parkingLotRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Parking lot not found with id: " + id));
        return convertToDTO(parkingLot);
    }

    public List<ParkingLotDTO> getParkingLotsByStatus(String status) {
        return parkingLotRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ParkingLotDTO createParkingLot(ParkingLotRequest parkingLotRequest) throws DataNotFoundException {
        // Kiểm tra và lấy chủ sở hữu
        User owner = userRepository.findById(parkingLotRequest.getOwnerId())
                .orElseThrow(() -> new DataNotFoundException("Owner not found with id: " + parkingLotRequest.getOwnerId()));

        ParkingLot parkingLot = new ParkingLot();
        parkingLot.setOwner(owner);
        parkingLot.setName(parkingLotRequest.getName());
        parkingLot.setAddress(parkingLotRequest.getAddress());
        parkingLot.setCapacity(parkingLotRequest.getCapacity());
        parkingLot.setAvailableSlots(parkingLotRequest.getAvailableSlots() != null ? parkingLotRequest.getAvailableSlots()
                : parkingLotRequest.getCapacity());
        parkingLot.setOperatingHours(parkingLotRequest.getOperatingHours());
        parkingLot.setHourlyRate(parkingLotRequest.getHourlyRate());
        parkingLot.setDailyRate(parkingLotRequest.getDailyRate());
        parkingLot.setVehicleTypes(
                parkingLotRequest.getVehicleTypes() != null ? String.join(",", parkingLotRequest.getVehicleTypes())
                        : "");
        parkingLot.setIsCovered(parkingLotRequest.getIsCovered());
        parkingLot.setStatus(parkingLotRequest.getStatus() != null ? parkingLotRequest.getStatus() : "ACTIVE");
        parkingLot.setCreatedAt(LocalDateTime.now());
        parkingLot.setUpdatedAt(LocalDateTime.now());

        ParkingLot savedParkingLot = parkingLotRepository.save(parkingLot);
        return convertToDTO(savedParkingLot);
    }

    public ParkingLotDTO updateParkingLot(Long id, ParkingLotDTO parkingLotDTO) throws DataNotFoundException {
        ParkingLot parkingLot = parkingLotRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Parking lot not found with id: " + id));

        if (parkingLotDTO.getOwnerId() != null) {
            User owner = userRepository.findById(parkingLotDTO.getOwnerId())
                    .orElseThrow(
                            () -> new DataNotFoundException("Owner not found with id: " + parkingLotDTO.getOwnerId()));
            parkingLot.setOwner(owner);
        }

        if (parkingLotDTO.getName() != null) {
            parkingLot.setName(parkingLotDTO.getName());
        }
        if (parkingLotDTO.getAddress() != null) {
            parkingLot.setAddress(parkingLotDTO.getAddress());
        }
        if (parkingLotDTO.getCapacity() != null) {
            parkingLot.setCapacity(parkingLotDTO.getCapacity());
        }
        if (parkingLotDTO.getAvailableSlots() != null) {
            parkingLot.setAvailableSlots(parkingLotDTO.getAvailableSlots());
        }
        if (parkingLotDTO.getOperatingHours() != null) {
            parkingLot.setOperatingHours(parkingLotDTO.getOperatingHours());
        }
        if (parkingLotDTO.getHourlyRate() != null) {
            parkingLot.setHourlyRate(parkingLotDTO.getHourlyRate());
        }
        if (parkingLotDTO.getDailyRate() != null) {
            parkingLot.setDailyRate(parkingLotDTO.getDailyRate());
        }
        if (parkingLotDTO.getVehicleTypes() != null) {
            parkingLot.setVehicleTypes(String.join(",", parkingLotDTO.getVehicleTypes()));
        }
        if (parkingLotDTO.getIsCovered() != null) {
            parkingLot.setIsCovered(parkingLotDTO.getIsCovered());
        }
        if (parkingLotDTO.getStatus() != null) {
            parkingLot.setStatus(parkingLotDTO.getStatus());
        }
        parkingLot.setUpdatedAt(LocalDateTime.now());

        ParkingLot updatedParkingLot = parkingLotRepository.save(parkingLot);
        return convertToDTO(updatedParkingLot);
    }

    public void deleteParkingLot(Long id) throws DataNotFoundException {
        if (!parkingLotRepository.existsById(id)) {
            throw new DataNotFoundException("Parking lot not found with id: " + id);
        }
        parkingLotRepository.deleteById(id);
    }

    public ParkingLotDTO updateParkingLotAvailability(Long id, Integer spotsChange) throws DataNotFoundException {
        ParkingLot parkingLot = parkingLotRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Parking lot not found with id: " + id));

        int newAvailableSlots = parkingLot.getAvailableSlots() + spotsChange;
        if (newAvailableSlots < 0) {
            throw new IllegalArgumentException("Not enough available spots in parking lot");
        }
        if (newAvailableSlots > parkingLot.getCapacity()) {
            throw new IllegalArgumentException("Available slots cannot exceed capacity");
        }

        parkingLot.setAvailableSlots(newAvailableSlots);
        parkingLot.setUpdatedAt(LocalDateTime.now());

        ParkingLot updatedParkingLot = parkingLotRepository.save(parkingLot);
        return convertToDTO(updatedParkingLot);
    }

    public ParkingLotDTO updateAvailableSlots(Long id, Integer availableSlots) throws DataNotFoundException {
        ParkingLot parkingLot = parkingLotRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Parking lot not found with id: " + id));

        if (availableSlots < 0) {
            throw new IllegalArgumentException("Available slots cannot be negative");
        }
        if (availableSlots > parkingLot.getCapacity()) {
            throw new IllegalArgumentException("Available slots cannot exceed capacity");
        }

        parkingLot.setAvailableSlots(availableSlots);
        parkingLot.setUpdatedAt(LocalDateTime.now());

        ParkingLot updatedParkingLot = parkingLotRepository.save(parkingLot);
        return convertToDTO(updatedParkingLot);
    }

    public List<ParkingLotDTO> findParkingLotsByOwnerId(Long ownerId) {
        List<ParkingLot> lots = parkingLotRepository.findByOwnerId(ownerId);
        return lots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ParkingLotDTO> findParkingLotsByFilters(
            String vehicleType,
            BigDecimal maxHourlyRate,
            BigDecimal maxDailyRate,
            Boolean isCovered) {

        List<ParkingLot> filteredLots = parkingLotRepository.findAll();

        if (vehicleType != null) {
            filteredLots = filteredLots.stream()
                    .filter(lot -> lot.getVehicleTypes().contains(vehicleType))
                    .collect(Collectors.toList());
        }

        if (maxHourlyRate != null) {
            filteredLots = filteredLots.stream()
                    .filter(lot -> lot.getHourlyRate().compareTo(maxHourlyRate) <= 0)
                    .collect(Collectors.toList());
        }

        if (maxDailyRate != null) {
            filteredLots = filteredLots.stream()
                    .filter(lot -> lot.getDailyRate().compareTo(maxDailyRate) <= 0)
                    .collect(Collectors.toList());
        }

        if (isCovered != null) {
            filteredLots = filteredLots.stream()
                    .filter(lot -> lot.getIsCovered().equals(isCovered))
                    .collect(Collectors.toList());
        }

        return filteredLots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ParkingLotDTO addEmployeeToParkingLot( EmployeeRequest employeeRequest) throws Exception {
        ParkingLot parkingLot = parkingLotRepository.findById(employeeRequest.getParkingLotId()).get();
        if (parkingLot == null) {
            throw new DataNotFoundException("Parking lot not found with id: " + employeeRequest.getParkingLotId());
        }
        Employee employee = new Employee();
        if(employeeRequest.getEmployeeId() != null) {
            employee = employeeRepository.findById(employeeRequest.getEmployeeId())
                .orElseThrow(() -> new DataNotFoundException("Employee not found with id: " + employeeRequest.getEmployeeId()));
        } else {
            employee = employeeService.createEmployee(employeeRequest);
        }
        
        employee.setParkingLot(parkingLot);
        employeeRepository.save(employee);
        return convertToDTO(parkingLot);
    }

    private ParkingLotDTO convertToDTO(ParkingLot parkingLot) {
        return ParkingLotDTO.fromParkingLot(parkingLot);
    }
}