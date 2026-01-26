package com.project.parking.service;

import com.project.parking.dto.ShiftAttendanceDTO;
import com.project.parking.model.EmployeeShift;
import com.project.parking.model.Shift;
import com.project.parking.model.ShiftAttendance;
import com.project.parking.repository.EmployeeShiftRepository;
import com.project.parking.repository.ShiftAttendanceRepository;
import com.project.parking.repository.ShiftRepository;
import com.project.parking.service.ShiftAttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiftAttendanceService {

    private final ShiftAttendanceRepository shiftAttendanceRepository;
    private final EmployeeShiftRepository employeeShiftRepository;
    private final ShiftRepository shiftRepository;

    public List<ShiftAttendanceDTO> getAllShiftAttendances() {
        return shiftAttendanceRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ShiftAttendanceDTO getShiftAttendanceById(Long id) {
        ShiftAttendance attendance = shiftAttendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shift attendance not found with ID: " + id));
        return convertToDTO(attendance);
    }

    public ShiftAttendanceDTO createShiftAttendance(ShiftAttendanceDTO attendanceDTO) {
        ShiftAttendance attendance = convertToEntity(attendanceDTO);
        attendance.setCreatedAt(LocalDateTime.now());
        attendance.setUpdatedAt(LocalDateTime.now());
        return convertToDTO(shiftAttendanceRepository.save(attendance));
    }

    public ShiftAttendanceDTO updateShiftAttendance(Long id, ShiftAttendanceDTO attendanceDTO) {
        ShiftAttendance existingAttendance = shiftAttendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shift attendance not found with ID: " + id));

        existingAttendance.setCheckInTime(attendanceDTO.getCheckInTime());
        existingAttendance.setCheckOutTime(attendanceDTO.getCheckOutTime());
        existingAttendance.setNotes(attendanceDTO.getNotes());
        existingAttendance.setStatus(attendanceDTO.getStatus());
        existingAttendance.setUpdatedAt(LocalDateTime.now());

        return convertToDTO(shiftAttendanceRepository.save(existingAttendance));
    }

    public void deleteShiftAttendance(Long id) {
        if (!shiftAttendanceRepository.existsById(id)) {
            throw new RuntimeException("Shift attendance not found with ID: " + id);
        }
        shiftAttendanceRepository.deleteById(id);
    }

    public ShiftAttendanceDTO checkIn(Long employeeShiftId) {
        EmployeeShift employeeShift = employeeShiftRepository.findById(employeeShiftId)
                .orElseThrow(() -> new RuntimeException("Employee shift not found with ID: " + employeeShiftId));
        // Kiểm tra xem đã có bản ghi điểm danh cho ca làm việc này chưa
        if (shiftAttendanceRepository.findByEmployeeShift(employeeShift).isPresent()) {
            throw new RuntimeException("Check-in already exists for this shift");
        }
        LocalDateTime shiftStart = LocalDateTime.of(employeeShift.getWorkDate(),
                employeeShift.getShift().getStartTime());
        if (shiftStart.isAfter(LocalDateTime.now().plusMinutes(30))) {
            throw new RuntimeException("Shift has not started yet");
        }
        ShiftAttendance attendance = new ShiftAttendance();
        attendance.setEmployeeShift(employeeShift);
        attendance.setCheckInTime(LocalDateTime.now());

        // Kiểm tra xem có đi trễ không
        LocalTime shiftStartTime = employeeShift.getShift().getStartTime();
        LocalTime currentTime = LocalTime.now();

        if (currentTime.isAfter(shiftStartTime.plusMinutes(15))) {
            attendance.setStatus("LATE");
        } else {
            attendance.setStatus("PRESENT");
        }

        attendance.setCreatedAt(LocalDateTime.now());
        attendance.setUpdatedAt(LocalDateTime.now());

        // Cập nhật trạng thái của ca làm việc
        employeeShift.setStatus("IN_PROGRESS");
        employeeShiftRepository.save(employeeShift);

        return convertToDTO(shiftAttendanceRepository.save(attendance));
    }

    public ShiftAttendanceDTO checkOut(Long employeeShiftId) {
        EmployeeShift employeeShift = employeeShiftRepository.findById(employeeShiftId)
                .orElseThrow(() -> new RuntimeException("Employee shift not found with ID: " + employeeShiftId));
        ShiftAttendance attendance = shiftAttendanceRepository.findByEmployeeShift(employeeShift)
                .orElseThrow(() -> new RuntimeException("Attendance not found for this employee shift"));
        if (attendance.getCheckOutTime() != null) {
            throw new RuntimeException("Already checked out");
        }

        attendance.setCheckOutTime(LocalDateTime.now());
        attendance.setUpdatedAt(LocalDateTime.now());
        LocalTime currentTime = LocalTime.now();
        if (attendance.getStatus().equals("LATE")) {
            attendance.setStatus("LATE");
        } else if (currentTime.isBefore(employeeShift.getShift().getEndTime())) {
            attendance.setStatus("EARLY");
        } else {
            attendance.setStatus("PRESENT");
        }
        // Cập nhật trạng thái của ca làm việc
        employeeShift.setStatus("COMPLETED");
        employeeShiftRepository.save(employeeShift);

        return convertToDTO(shiftAttendanceRepository.save(attendance));
    }

    public List<ShiftAttendanceDTO> getAttendancesByEmployeeId(Long employeeId, LocalDateTime startDate,
            LocalDateTime endDate) {
        return shiftAttendanceRepository.findByEmployeeIdAndDateRange(employeeId, startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ShiftAttendanceDTO> getAttendancesByParkingLotId(Long parkingLotId, LocalDateTime startDate,
            LocalDateTime endDate) {
        return shiftAttendanceRepository.findByParkingLotIdAndDateRange(parkingLotId, startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ShiftAttendanceDTO markAbsent(Long employeeShiftId, String reason) {
        EmployeeShift employeeShift = employeeShiftRepository.findById(employeeShiftId)
                .orElseThrow(() -> new RuntimeException("Employee shift not found with ID: " + employeeShiftId));

        // Kiểm tra xem đã có bản ghi điểm danh cho ca làm việc này chưa
        if (shiftAttendanceRepository.findByEmployeeShift(employeeShift).isPresent()) {
            throw new RuntimeException("Attendance record already exists for this shift");
        }

        ShiftAttendance attendance = new ShiftAttendance();
        attendance.setEmployeeShift(employeeShift);
        attendance.setStatus("ABSENT");
        attendance.setNotes(reason);
        attendance.setCreatedAt(LocalDateTime.now());
        attendance.setUpdatedAt(LocalDateTime.now());

        // Cập nhật trạng thái của ca làm việc
        employeeShift.setStatus("ABSENT");
        employeeShiftRepository.save(employeeShift);

        return convertToDTO(shiftAttendanceRepository.save(attendance));
    }

    @Scheduled(cron = "0 */30 * * * *") // Chạy mỗi 30 phút
    public void markAbsentEmployees() {
        LocalTime currentTime = LocalTime.now();
        log.info("markAbsentEmployees: {}", currentTime);
        // Lấy tất cả ca làm đã kết thúc trong ngày hiện tại
        List<Shift> completedShifts = shiftRepository.findCompletedShiftsForTime(currentTime, LocalTime.MIDNIGHT,
                LocalTime.of(23, 30));

        for (Shift shift : completedShifts) {
            // Xử lý ca cuối ngày đặc biệt (ca kết thúc lúc 24h/0h)
            boolean isEndOfDayShift = shift.getEndTime().equals(LocalTime.of(0, 0)) ||
                    shift.getEndTime().equals(LocalTime.MIDNIGHT);

            // Nếu là ca kết thúc vào 24h và thời gian hiện tại chưa đến gần cuối ngày, bỏ
            // qua
            if (isEndOfDayShift && currentTime.isBefore(LocalTime.of(23, 30))) {
                continue;
            }
            // Tìm tất cả nhân viên được phân công vào ca này
            List<EmployeeShift> employeeShifts = employeeShiftRepository.findByShiftIdAndWorkDate(shift.getId(),
                    LocalDate.now());

            for (EmployeeShift employeeShift : employeeShifts) {
                // Kiểm tra xem nhân viên đã có bản ghi điểm danh chưa
                Optional<ShiftAttendance> attendance = shiftAttendanceRepository
                        .findByEmployeeShiftId(employeeShift.getId());

                // Nếu chưa có bản ghi điểm danh, tạo một bản ghi mới với trạng thái ABSENT
                if (attendance.isEmpty()) {
                    ShiftAttendance newAttendance = new ShiftAttendance();
                    newAttendance.setEmployeeShift(employeeShift);
                    newAttendance.setStatus("ABSENT");
                    newAttendance.setNotes("Tự động tạo vì đã quá thời gian check in");
                    // Có thể đặt thêm thông tin khác nếu cần
                    employeeShift.setStatus("ABSENT");

                    shiftAttendanceRepository.save(newAttendance);
                }
            }
        }
    }

    public void markAbsentEmployeesForDate(LocalDate date) {
        LocalTime currentTime = LocalTime.now();
        log.info("markAbsentEmployees: {}", currentTime);
        // Lấy tất cả ca làm đã kết thúc trong ngày hiện tại
        List<Shift> completedShifts;
        if (date.isBefore(LocalDate.now())) {
            completedShifts = shiftRepository.findAll();
        } else {
            completedShifts = shiftRepository.findCompletedShiftsForTime(currentTime, LocalTime.MIDNIGHT,
                    LocalTime.of(23, 30));
        }

        for (Shift shift : completedShifts) {
            // Xử lý ca cuối ngày đặc biệt (ca kết thúc lúc 24h/0h)
            boolean isEndOfDayShift = shift.getEndTime().equals(LocalTime.of(0, 0)) ||
                    shift.getEndTime().equals(LocalTime.MIDNIGHT);

            // Nếu là ca kết thúc vào 24h và thời gian hiện tại chưa đến gần cuối ngày, bỏ
            // qua
            if (isEndOfDayShift && currentTime.isBefore(LocalTime.of(23, 30))) {
                continue;
            }
            // Tìm tất cả nhân viên được phân công vào ca này
            List<EmployeeShift> employeeShifts = employeeShiftRepository.findByShiftIdAndWorkDate(shift.getId(), date);

            for (EmployeeShift employeeShift : employeeShifts) {
                // Kiểm tra xem nhân viên đã có bản ghi điểm danh chưa
                Optional<ShiftAttendance> attendance = shiftAttendanceRepository
                        .findByEmployeeShiftId(employeeShift.getId());

                // Nếu chưa có bản ghi điểm danh, tạo một bản ghi mới với trạng thái ABSENT
                if (attendance.isEmpty()) {
                    ShiftAttendance newAttendance = new ShiftAttendance();
                    newAttendance.setEmployeeShift(employeeShift);
                    newAttendance.setStatus("ABSENT");
                    newAttendance.setNotes("Tự động tạo bởi admin cho dữ liệu quá khứ");
                    // Có thể đặt thêm thông tin khác nếu cần
                    employeeShift.setStatus("ABSENT");
                    shiftAttendanceRepository.save(newAttendance);
                }
            }
        }
    }

    /**
     * Phương thức để admin quét và cập nhật trạng thái điểm danh cho các ca làm
     * việc
     * trong một khoảng thời gian nhất định trong quá khứ
     * 
     * @param fromDate Ngày bắt đầu quét
     * @param toDate   Ngày kết thúc quét
     * @return Số lượng bản ghi đã được cập nhật
     */
    public int processHistoricalAttendances(LocalDate fromDate, LocalDate toDate) {
        log.info("Đang xử lý điểm danh cho khoảng thời gian từ {} đến {}", fromDate, toDate);

        if (fromDate.isAfter(toDate)) {
            throw new RuntimeException("Ngày bắt đầu không thể sau ngày kết thúc");
        }

        int updatedRecords = 0;
        LocalDate currentDate = fromDate;

        // Xử lý từng ngày trong khoảng thời gian
        while (!currentDate.isAfter(toDate)) {
            log.info("Đang xử lý điểm danh cho ngày: {}", currentDate);

            // Lấy tất cả ca làm việc trong ngày
            List<Shift> allShifts = shiftRepository.findAll();

            for (Shift shift : allShifts) {
                // Tìm tất cả nhân viên được phân công vào ca này trong ngày đang xét
                List<EmployeeShift> employeeShifts = employeeShiftRepository.findByShiftIdAndWorkDate(shift.getId(),
                        currentDate);

                for (EmployeeShift employeeShift : employeeShifts) {
                    // Kiểm tra xem nhân viên đã có bản ghi điểm danh chưa
                    Optional<ShiftAttendance> attendance = shiftAttendanceRepository
                            .findByEmployeeShiftId(employeeShift.getId());

                    // Nếu chưa có bản ghi điểm danh, tạo một bản ghi mới với trạng thái ABSENT
                    if (attendance.isEmpty()) {
                        ShiftAttendance newAttendance = new ShiftAttendance();
                        newAttendance.setEmployeeShift(employeeShift);
                        newAttendance.setStatus("ABSENT");
                        newAttendance.setNotes("Tự động tạo bởi admin cho dữ liệu quá khứ");
                        newAttendance.setCreatedAt(LocalDateTime.now());
                        newAttendance.setUpdatedAt(LocalDateTime.now());

                        shiftAttendanceRepository.save(newAttendance);

                        // Cập nhật trạng thái của ca làm việc
                        employeeShift.setStatus("ABSENT");
                        employeeShiftRepository.save(employeeShift);

                        updatedRecords++;
                    }
                }
            }

            // Chuyển sang ngày tiếp theo
            currentDate = currentDate.plusDays(1);
        }

        log.info("Đã hoàn thành xử lý điểm danh. Số bản ghi đã được cập nhật: {}", updatedRecords);
        return updatedRecords;
    }

    private ShiftAttendanceDTO convertToDTO(ShiftAttendance attendance) {
        EmployeeShift employeeShift = attendance.getEmployeeShift();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String shiftTime = employeeShift.getShift().getStartTime().format(timeFormatter) + "-" +
                employeeShift.getShift().getEndTime().format(timeFormatter);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String workDate = employeeShift.getWorkDate().format(dateFormatter);

        ShiftAttendanceDTO dto = new ShiftAttendanceDTO();
        dto.setId(attendance.getId());
        dto.setEmployeeShiftId(employeeShift.getId());
        dto.setEmployeeId(employeeShift.getEmployee().getId());
        dto.setEmployeeName(employeeShift.getEmployee().getUser().getFullname());
        dto.setShiftName(employeeShift.getShift().getShiftName());
        dto.setShiftTime(shiftTime);
        dto.setWorkDate(workDate);
        dto.setCheckInTime(attendance.getCheckInTime());
        dto.setCheckOutTime(attendance.getCheckOutTime());
        dto.setNotes(attendance.getNotes());
        dto.setStatus(attendance.getStatus());
        dto.setParkingLotId(employeeShift.getParkingLot().getId());
        dto.setParkingLotName(employeeShift.getParkingLot().getName());
        dto.setCreatedAt(attendance.getCreatedAt());
        dto.setUpdatedAt(attendance.getUpdatedAt());

        return dto;
    }

    private ShiftAttendance convertToEntity(ShiftAttendanceDTO dto) {
        ShiftAttendance attendance = new ShiftAttendance();

        if (dto.getId() != null) {
            attendance.setId(dto.getId());
        }

        EmployeeShift employeeShift = employeeShiftRepository.findById(dto.getEmployeeShiftId())
                .orElseThrow(
                        () -> new RuntimeException("Employee shift not found with ID: " + dto.getEmployeeShiftId()));

        attendance.setEmployeeShift(employeeShift);
        attendance.setCheckInTime(dto.getCheckInTime());
        attendance.setCheckOutTime(dto.getCheckOutTime());
        attendance.setNotes(dto.getNotes());
        attendance.setStatus(dto.getStatus() != null ? dto.getStatus() : "PRESENT");

        return attendance;
    }
}
