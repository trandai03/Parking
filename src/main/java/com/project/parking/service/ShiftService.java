package com.project.parking.service;

import com.project.parking.dto.ShiftDTO;
import com.project.parking.model.Shift;
import com.project.parking.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftRepository shiftRepository;

    public List<ShiftDTO> getAllShifts() {
        return shiftRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ShiftDTO getShiftById(Long id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shift not found with ID: " + id));
        return convertToDTO(shift);
    }

    public ShiftDTO createShift(ShiftDTO shiftDTO) {
        Shift shift = convertToEntity(shiftDTO);
        shift.setCreatedAt(LocalDateTime.now());
        shift.setUpdatedAt(LocalDateTime.now());
        return convertToDTO(shiftRepository.save(shift));
    }

    public ShiftDTO updateShift(Long id, ShiftDTO shiftDTO) {
        Shift existingShift = shiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shift not found with ID: " + id));

        existingShift.setShiftName(shiftDTO.getShiftName());
        existingShift.setStartTime(shiftDTO.getStartTime());
        existingShift.setEndTime(shiftDTO.getEndTime());
        existingShift.setDescription(shiftDTO.getDescription());
        existingShift.setStatus(shiftDTO.getStatus());
        existingShift.setUpdatedAt(LocalDateTime.now());

        return convertToDTO(shiftRepository.save(existingShift));
    }

    public void deleteShift(Long id) {
        if (!shiftRepository.existsById(id)) {
            throw new RuntimeException("Shift not found with ID: " + id);
        }
        shiftRepository.deleteById(id);
    }

    public List<ShiftDTO> getActiveShifts() {
        return shiftRepository.findByStatus("ACTIVE").stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ShiftDTO getShiftByName(String shiftName) {
        Shift shift = shiftRepository.findByShiftName(shiftName)
                .orElseThrow(() -> new RuntimeException("Shift not found with name: " + shiftName));
        return convertToDTO(shift);
    }

    private ShiftDTO convertToDTO(Shift shift) {
        return ShiftDTO.builder()
                .id(shift.getId())
                .shiftName(shift.getShiftName())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .description(shift.getDescription())
                .status(shift.getStatus())
                .createdAt(shift.getCreatedAt())
                .updatedAt(shift.getUpdatedAt())
                .build();
    }

    private Shift convertToEntity(ShiftDTO shiftDTO) {
        Shift shift = new Shift();
        shift.setId(shiftDTO.getId());
        shift.setShiftName(shiftDTO.getShiftName());
        shift.setStartTime(shiftDTO.getStartTime());
        shift.setEndTime(shiftDTO.getEndTime());
        shift.setDescription(shiftDTO.getDescription());
        shift.setStatus(shiftDTO.getStatus() != null ? shiftDTO.getStatus() : "ACTIVE");
        return shift;
    }
}