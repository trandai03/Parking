package com.project.parking.service;

import com.project.parking.dto.EmployeeShiftDTO;
import com.project.parking.model.Employee;
import com.project.parking.model.EmployeeShift;
import com.project.parking.model.ParkingLot;
import com.project.parking.model.Shift;
import com.project.parking.repository.EmployeeRepository;
import com.project.parking.repository.EmployeeShiftRepository;
import com.project.parking.repository.ParkingLotRepository;
import com.project.parking.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeShiftService {

    private final EmployeeShiftRepository employeeShiftRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final ParkingLotRepository parkingLotRepository;

    public List<EmployeeShiftDTO> getAllEmployeeShifts(Long shiftId, LocalDate workDate) {

        return employeeShiftRepository.findAll(shiftId, workDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EmployeeShiftDTO getEmployeeShiftById(Long id) {
        EmployeeShift employeeShift = employeeShiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee shift not found with ID: " + id));
        return convertToDTO(employeeShift);
    }

    public EmployeeShiftDTO createEmployeeShift(EmployeeShiftDTO employeeShiftDTO) {
        EmployeeShift existingShift = employeeShiftRepository.findByEmployeeAndShiftAndWorkDate(employeeShiftDTO.getEmployeeId(), employeeShiftDTO.getShiftId(), employeeShiftDTO.getWorkDate());
        EmployeeShift employeeShift = convertToEntity(employeeShiftDTO);
        if (existingShift != null) {
            throw new RuntimeException("Employee shift already exists with the same employee, shift, and work date");
        }
        if (employeeShift.getIsRecurring() && employeeShift.getDayOfWeek() != null) {
            if (employeeShift.getWorkDate() == null) {
                // Nếu đây là ca lặp lại, tìm ngày trong tuần đầu tiên kể từ hôm nay
                LocalDate today = LocalDate.now();
                employeeShift.setWorkDate(getNextDayOfWeek(today, employeeShift.getDayOfWeek()));
            } else {
                // Nếu đã có ngày, đặt dayOfWeek dựa trên ngày đó
                employeeShift.setDayOfWeek(employeeShift.getWorkDate().getDayOfWeek());
            }
        }

        employeeShift.setCreatedAt(LocalDateTime.now());
        employeeShift.setUpdatedAt(LocalDateTime.now());
        return convertToDTO(employeeShiftRepository.save(employeeShift));
    }

    public EmployeeShiftDTO updateEmployeeShift(Long id, EmployeeShiftDTO employeeShiftDTO) {
        EmployeeShift existingShift = employeeShiftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee shift not found with ID: " + id));

        Employee employee = employeeRepository.findById(employeeShiftDTO.getEmployeeId())
                .orElseThrow(
                        () -> new RuntimeException("Employee not found with ID: " + employeeShiftDTO.getEmployeeId()));

        Shift shift = shiftRepository.findById(employeeShiftDTO.getShiftId())
                .orElseThrow(() -> new RuntimeException("Shift not found with ID: " + employeeShiftDTO.getShiftId()));

        ParkingLot parkingLot = parkingLotRepository.findById(employeeShiftDTO.getParkingLotId())
                .orElseThrow(() -> new RuntimeException(
                        "Parking lot not found with ID: " + employeeShiftDTO.getParkingLotId()));

        existingShift.setEmployee(employee);
        existingShift.setShift(shift);
        existingShift.setWorkDate(employeeShiftDTO.getWorkDate());
        existingShift.setDayOfWeek(employeeShiftDTO.getDayOfWeek());
        existingShift.setIsRecurring(employeeShiftDTO.getIsRecurring());
        existingShift.setStatus(employeeShiftDTO.getStatus());
        existingShift.setParkingLot(parkingLot);
        existingShift.setUpdatedAt(LocalDateTime.now());

        return convertToDTO(employeeShiftRepository.save(existingShift));
    }

    public void deleteEmployeeShift(Long id) {
        if (!employeeShiftRepository.existsById(id)) {
            throw new RuntimeException("Employee shift not found with ID: " + id);
        }
        employeeShiftRepository.deleteById(id);
    }

    public List<EmployeeShiftDTO> getEmployeeShiftsByEmployeeId(Long employeeId) {
        return employeeShiftRepository.findByEmployeeId(employeeId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EmployeeShiftDTO> getEmployeeShiftsByShiftId(Long shiftId) {
        return employeeShiftRepository.findByShiftId(shiftId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EmployeeShiftDTO> getEmployeeShiftsByWorkDate(LocalDate workDate) {
        return employeeShiftRepository.findByWorkDate(workDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EmployeeShiftDTO> getEmployeeShiftsByDateRange(Long employeeId, LocalDate startDate,
            LocalDate endDate) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        return employeeShiftRepository.findByEmployeeAndWorkDateBetween(employee, startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EmployeeShiftDTO> getEmployeeShiftsByParkingLotAndDate(Long parkingLotId, LocalDate workDate) {
        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new RuntimeException("Parking lot not found with ID: " + parkingLotId));

        return employeeShiftRepository.findByParkingLotAndWorkDate(parkingLot, workDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void generateRecurringShifts(LocalDate fromDate, LocalDate toDate) {
        // Tìm tất cả ca làm việc lặp lại
        List<EmployeeShift> recurringShifts = employeeShiftRepository
                .findByIsRecurringTrueAndDayOfWeek(fromDate.getDayOfWeek());

        for (EmployeeShift recurringShift : recurringShifts) {
            LocalDate currentDate = fromDate;

            // Tạo ca làm việc cho mỗi ngày trong phạm vi
            while (!currentDate.isAfter(toDate)) {
                // Kiểm tra xem ngày hiện tại có phải là ngày trong tuần phù hợp không
                if (currentDate.getDayOfWeek() == recurringShift.getDayOfWeek()) {
                    // Kiểm tra xem đã có ca làm việc nào được tạo cho ngày này chưa
                    List<EmployeeShift> existingShifts = employeeShiftRepository.findByParkingLotAndShiftAndWorkDate(
                            recurringShift.getParkingLot(), recurringShift.getShift(), currentDate);

                    // Nếu chưa có, tạo ca làm việc mới
                    if (existingShifts.isEmpty()) {
                        EmployeeShift newShift = new EmployeeShift();
                        newShift.setEmployee(recurringShift.getEmployee());
                        newShift.setShift(recurringShift.getShift());
                        newShift.setWorkDate(currentDate);
                        newShift.setDayOfWeek(currentDate.getDayOfWeek());
                        newShift.setIsRecurring(false); // Ca làm việc được tạo từ mẫu lặp lại
                        newShift.setStatus("SCHEDULED");
                        newShift.setParkingLot(recurringShift.getParkingLot());
                        newShift.setCreatedAt(LocalDateTime.now());
                        newShift.setUpdatedAt(LocalDateTime.now());

                        employeeShiftRepository.save(newShift);
                    }
                }

                currentDate = currentDate.plusDays(1);
            }
        }
    }

    private EmployeeShiftDTO convertToDTO(EmployeeShift employeeShift) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String shiftTime = employeeShift.getShift().getStartTime().format(timeFormatter) + "-" +
                employeeShift.getShift().getEndTime().format(timeFormatter);

        return EmployeeShiftDTO.builder()
                .id(employeeShift.getId())
                .employeeId(employeeShift.getEmployee().getId())
                .employeeName(employeeShift.getEmployee().getUser().getFullname())
                .shiftId(employeeShift.getShift().getId())
                .shiftName(employeeShift.getShift().getShiftName())
                .shiftTime(shiftTime)
                .workDate(employeeShift.getWorkDate())
                .dayOfWeek(employeeShift.getDayOfWeek())
                .isRecurring(employeeShift.getIsRecurring())
                .status(employeeShift.getStatus())
                .parkingLotId(employeeShift.getParkingLot().getId())
                .parkingLotName(employeeShift.getParkingLot().getName())
                .createdAt(employeeShift.getCreatedAt())
                .updatedAt(employeeShift.getUpdatedAt())
                .build();
    }

    private EmployeeShift convertToEntity(EmployeeShiftDTO dto) {
        EmployeeShift employeeShift = new EmployeeShift();

        if (dto.getId() != null) {
            employeeShift.setId(dto.getId());
        }

        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + dto.getEmployeeId()));

        Shift shift = shiftRepository.findById(dto.getShiftId())
                .orElseThrow(() -> new RuntimeException("Shift not found with ID: " + dto.getShiftId()));

        ParkingLot parkingLot = parkingLotRepository.findById(dto.getParkingLotId())
                .orElseThrow(() -> new RuntimeException("Parking lot not found with ID: " + dto.getParkingLotId()));

        employeeShift.setEmployee(employee);
        employeeShift.setShift(shift);
        employeeShift.setWorkDate(dto.getWorkDate());
        employeeShift.setDayOfWeek(dto.getDayOfWeek());
        employeeShift.setIsRecurring(dto.getIsRecurring() != null ? dto.getIsRecurring() : false);
        employeeShift.setStatus(dto.getStatus() != null ? dto.getStatus() : "SCHEDULED");
        employeeShift.setParkingLot(parkingLot);

        return employeeShift;
    }

    private LocalDate getNextDayOfWeek(LocalDate startDate, DayOfWeek dayOfWeek) {
        LocalDate date = startDate;
        while (date.getDayOfWeek() != dayOfWeek) {
            date = date.plusDays(1);
        }
        return date;
    }
}