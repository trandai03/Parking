package com.project.parking.controller;

import com.project.parking.dto.EmployeeShiftDTO;
import com.project.parking.service.EmployeeShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employee-shifts")
@RequiredArgsConstructor
@Tag(name = "Employee Shift Controller", description = "Quản lý phân công ca làm việc cho nhân viên")
public class EmployeeShiftController {

    private final EmployeeShiftService employeeShiftService;

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả phân công ca làm việc theo ca làm việc và ngày làm việc ", description = "API này dùng để lấy danh sách tất cả phân công ca làm việc trong hệ thống.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<List<EmployeeShiftDTO>> getAllEmployeeShifts(
            @RequestParam(required = false) Long shiftId,
            @RequestParam(required = false) LocalDate workDate) {
        return ResponseEntity.ok(employeeShiftService.getAllEmployeeShifts( shiftId, workDate));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin phân công ca làm việc theo ID", description = "API này dùng để lấy thông tin chi tiết của một phân công ca làm việc dựa vào ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy phân công ca làm việc")
    })
    public ResponseEntity<EmployeeShiftDTO> getEmployeeShiftById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeShiftService.getEmployeeShiftById(id));
    }

    @PostMapping
    @Operation(summary = "Tạo mới phân công ca làm việc", description = "API này dùng để tạo mới một phân công ca làm việc cho nhân viên.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo mới thành công")
    })
    public ResponseEntity<EmployeeShiftDTO> createEmployeeShift(@RequestBody EmployeeShiftDTO employeeShiftDTO) {
        return new ResponseEntity<>(employeeShiftService.createEmployeeShift(employeeShiftDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật phân công ca làm việc", description = "API này dùng để cập nhật thông tin của phân công ca làm việc dựa vào ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy phân công ca làm việc")
    })
    public ResponseEntity<EmployeeShiftDTO> updateEmployeeShift(@PathVariable Long id,
            @RequestBody EmployeeShiftDTO employeeShiftDTO) {
        return ResponseEntity.ok(employeeShiftService.updateEmployeeShift(id, employeeShiftDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa phân công ca làm việc", description = "API này dùng để xóa một phân công ca làm việc khỏi hệ thống dựa vào ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy phân công ca làm việc")
    })
    public ResponseEntity<Void> deleteEmployeeShift(@PathVariable Long id) {
        employeeShiftService.deleteEmployeeShift(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Lấy danh sách phân công ca làm việc của một nhân viên", description = "API này dùng để lấy tất cả phân công ca làm việc của một nhân viên dựa vào ID nhân viên.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên")
    })
    public ResponseEntity<List<EmployeeShiftDTO>> getEmployeeShiftsByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(employeeShiftService.getEmployeeShiftsByEmployeeId(employeeId));
    }

    

    @GetMapping("/employee/{employeeId}/date-range")
    @Operation(summary = "Lấy danh sách phân công ca làm việc của nhân viên trong khoảng thời gian", description = "API này dùng để lấy tất cả phân công ca làm việc của một nhân viên trong khoảng thời gian từ ngày bắt đầu đến ngày kết thúc.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên")
    })
    public ResponseEntity<List<EmployeeShiftDTO>> getEmployeeShiftsByDateRange(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(employeeShiftService.getEmployeeShiftsByDateRange(employeeId, startDate, endDate));
    }

    @GetMapping("/parking-lot/{parkingLotId}/date/{workDate}")
    @Operation(summary = "Lấy danh sách phân công ca làm việc tại một bãi đỗ xe vào một ngày cụ thể", description = "API này dùng để lấy tất cả phân công ca làm việc tại một bãi đỗ xe vào một ngày cụ thể.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bãi đỗ xe")
    })
    public ResponseEntity<List<EmployeeShiftDTO>> getEmployeeShiftsByParkingLotAndDate(
            @PathVariable Long parkingLotId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate) {
        return ResponseEntity.ok(employeeShiftService.getEmployeeShiftsByParkingLotAndDate(parkingLotId, workDate));
    }

    @PostMapping("/generate-recurring")
    @Operation(summary = "Tạo các phân công ca lặp lại trong khoảng thời gian", description = "API này dùng để tạo tự động các phân công ca làm việc dựa trên các quy tắc lặp lại đã cấu hình trong khoảng thời gian từ ngày bắt đầu đến ngày kết thúc.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo lịch thành công")
    })
    public ResponseEntity<Void> generateRecurringShifts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        employeeShiftService.generateRecurringShifts(fromDate, toDate);
        return ResponseEntity.ok().build();
    }
}