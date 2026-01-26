package com.project.parking.service;

import com.project.parking.dto.VehicleDTO;
import com.project.parking.exceptions.DataNotFoundException;
import com.project.parking.model.User;
import com.project.parking.model.Vehicle;
import com.project.parking.repository.UserRepository;
import com.project.parking.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleService {
    
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    
    public List<VehicleDTO> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public VehicleDTO getVehicleById(Long id) throws DataNotFoundException {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Vehicle not found with id: " + id));
        return convertToDTO(vehicle);
    }
    
    public VehicleDTO getVehicleByLicensePlate(String licensePlate) throws DataNotFoundException {
        Vehicle vehicle = vehicleRepository.findByLicensePlate(licensePlate)
                .orElseThrow(() -> new DataNotFoundException("Vehicle not found with license plate: " + licensePlate));
        return convertToDTO(vehicle);
    }
    
    public VehicleDTO createVehicle(VehicleDTO vehicleDTO) throws DataNotFoundException {
        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(vehicleDTO.getLicensePlate());
        vehicle.setVehicleType(vehicleDTO.getVehicleType());
        vehicle.setCreatedAt(LocalDateTime.now());
        vehicle.setUpdatedAt(LocalDateTime.now());
        
        if (vehicleDTO.getUserId() != null) {
            User user = userRepository.findById(vehicleDTO.getUserId())
                    .orElseThrow(() -> new DataNotFoundException("User not found with id: " + vehicleDTO.getUserId()));
            vehicle.setUser(user);
        }
        
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return convertToDTO(savedVehicle);
    }
    
    public VehicleDTO updateVehicle(Long id, VehicleDTO vehicleDTO) throws DataNotFoundException {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Vehicle not found with id: " + id));
        
        vehicle.setLicensePlate(vehicleDTO.getLicensePlate());
        vehicle.setVehicleType(vehicleDTO.getVehicleType());
        vehicle.setUpdatedAt(LocalDateTime.now());
        
        if (vehicleDTO.getUserId() != null) {
            User user = userRepository.findById(vehicleDTO.getUserId())
                    .orElseThrow(() -> new DataNotFoundException("User not found with id: " + vehicleDTO.getUserId()));
            vehicle.setUser(user);
        }
        
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return convertToDTO(updatedVehicle);
    }
    
    public void deleteVehicle(Long id) throws DataNotFoundException {
        if (!vehicleRepository.existsById(id)) {
            throw new DataNotFoundException("Vehicle not found with id: " + id);
        }
        vehicleRepository.deleteById(id);
    }
    
    private VehicleDTO convertToDTO(Vehicle vehicle) {
        return VehicleDTO.builder()
                .id(vehicle.getId())
                .userId(vehicle.getUser() != null ? vehicle.getUser().getId() : null)
                .licensePlate(vehicle.getLicensePlate())
                .vehicleType(vehicle.getVehicleType())
                .build();
    }
}