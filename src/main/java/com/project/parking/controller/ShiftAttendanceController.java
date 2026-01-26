package com.project.parking.controller;

import com.project.parking.dto.ShiftAttendanceDTO;
import com.project.parking.service.ShiftAttendanceService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
@Tag(name = "Shift Attendance Controller", description = "Quản lý điểm danh ca làm việc của nhân viên")
public class ShiftAttendanceController {

        private final ShiftAttendanceService shiftAttendanceService;

        @GetMapping
        @Operation(summary = "Lấy danh sách tất cả điểm danh ca làm việc", description = "API này dùng để lấy danh sách tất cả các bản ghi điểm danh ca làm việc trong hệ thống.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
        })
        public ResponseEntity<List<ShiftAttendanceDTO>> getAllAttendances() {
                return ResponseEntity.ok(shiftAttendanceService.getAllShiftAttendances());
        }

        @GetMapping("/{id}")
        @Operation(summary = "Lấy thông tin điểm danh theo ID", description = "API này dùng để lấy thông tin chi tiết của một bản ghi điểm danh dựa vào ID.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy bản ghi điểm danh")
        })
        public ResponseEntity<ShiftAttendanceDTO> getAttendanceById(@PathVariable Long id) {
                return ResponseEntity.ok(shiftAttendanceService.getShiftAttendanceById(id));
        }

        @PostMapping
        @Operation(summary = "Tạo mới bản ghi điểm danh", description = "API này dùng để tạo mới một bản ghi điểm danh ca làm việc.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Tạo mới thành công")
        })
        public ResponseEntity<ShiftAttendanceDTO> createAttendance(@RequestBody ShiftAttendanceDTO attendanceDTO) {
                return new ResponseEntity<>(shiftAttendanceService.createShiftAttendance(attendanceDTO),
                                HttpStatus.CREATED);
        }

        @PutMapping("/{id}")
        @Operation(summary = "Cập nhật bản ghi điểm danh", description = "API này dùng để cập nhật thông tin của một bản ghi điểm danh dựa vào ID.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy bản ghi điểm danh")
        })
        public ResponseEntity<ShiftAttendanceDTO> updateAttendance(@PathVariable Long id,
                        @RequestBody ShiftAttendanceDTO attendanceDTO) {
                return ResponseEntity.ok(shiftAttendanceService.updateShiftAttendance(id, attendanceDTO));
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Xóa bản ghi điểm danh", description = "API này dùng để xóa một bản ghi điểm danh khỏi hệ thống dựa vào ID.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Xóa thành công"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy bản ghi điểm danh")
        })
        public ResponseEntity<Void> deleteAttendance(@PathVariable Long id) {
                shiftAttendanceService.deleteShiftAttendance(id);
                return ResponseEntity.noContent().build();
        }

        
        @Operation(summary = "Check-in ca làm việc", description = "API này dùng để nhân viên check-in khi bắt đầu ca làm việc.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Check-in thành công"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy ca làm việc"),
                        @ApiResponse(responseCode = "400", description = "Đã check-in trước đó")
        })
        @PostMapping("/check-in")
        public ResponseEntity<ShiftAttendanceDTO> checkIn(@RequestParam Long employeeShiftId) {
                return ResponseEntity.ok(shiftAttendanceService.checkIn(employeeShiftId));
        }

        
        @Operation(summary = "Check-out ca làm việc", description = "API này dùng để nhân viên check-out khi kết thúc ca làm việc.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Check-out thành công"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy bản ghi điểm danh"),
                        @ApiResponse(responseCode = "400", description = "Đã check-out trước đó")
        })
        @PostMapping("/check-out/{employeeShiftId}")
        public ResponseEntity<ShiftAttendanceDTO> checkOut(@PathVariable Long employeeShiftId) {
                return ResponseEntity.ok(shiftAttendanceService.checkOut(employeeShiftId));
        }

        @GetMapping("/employee/{employeeId}")
        @Operation(summary = "Lấy danh sách điểm danh của một nhân viên trong khoảng thời gian", description = "API này dùng để lấy tất cả bản ghi điểm danh của một nhân viên trong khoảng thời gian từ ngày bắt đầu đến ngày kết thúc.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên")
        })
        public ResponseEntity<List<ShiftAttendanceDTO>> getAttendancesByEmployeeId(
                        @PathVariable Long employeeId,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
                return ResponseEntity
                                .ok(shiftAttendanceService.getAttendancesByEmployeeId(employeeId, startDate, endDate));
        }

        @GetMapping("/parking-lot/{parkingLotId}")
        @Operation(summary = "Lấy danh sách điểm danh tại một bãi đỗ xe trong khoảng thời gian", description = "API này dùng để lấy tất cả bản ghi điểm danh tại một bãi đỗ xe trong khoảng thời gian từ ngày bắt đầu đến ngày kết thúc.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy bãi đỗ xe")
        })
        public ResponseEntity<List<ShiftAttendanceDTO>> getAttendancesByParkingLotId(
                        @PathVariable Long parkingLotId,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
                return ResponseEntity.ok(
                                shiftAttendanceService.getAttendancesByParkingLotId(parkingLotId, startDate, endDate));
        }

        @PostMapping("/absent")
        @Operation(summary = "Đánh dấu nhân viên vắng mặt", description = "API này dùng để đánh dấu nhân viên vắng mặt trong một ca làm việc cụ thể và ghi lại lý do vắng mặt.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Đánh dấu vắng mặt thành công"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy ca làm việc"),
                        @ApiResponse(responseCode = "400", description = "Đã có bản ghi điểm danh cho ca làm việc này")
        })
        public ResponseEntity<ShiftAttendanceDTO> markAbsent(@RequestBody Map<String, Object> request) {
                Long employeeShiftId = Long.valueOf(request.get("employeeShiftId").toString());
                String reason = request.get("reason").toString();
                return ResponseEntity.ok(shiftAttendanceService.markAbsent(employeeShiftId, reason));
        }

        @PostMapping("/mark-absent-for-date")
        @Operation(summary = "Đánh dấu nhân viên vắng mặt cho ngày hiện tại", description = "API này dùng để đánh dấu nhân viên vắng mặt cho ngày hiện tại.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Đánh dấu vắng mặt thành công")
        })
        public ResponseEntity<?> markAbsentForDate(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
                shiftAttendanceService.markAbsentEmployeesForDate(date);
                return ResponseEntity.ok().build();
        }

        @PostMapping("/process-historical")
        @Operation(summary = "Xử lý điểm danh cho dữ liệu quá khứ", description = "API này dùng để admin quét và xử lý điểm danh cho các ca làm việc trong khoảng thời gian quá khứ.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Xử lý dữ liệu quá khứ thành công"),
                        @ApiResponse(responseCode = "400", description = "Ngày bắt đầu không thể sau ngày kết thúc")
        })
        public ResponseEntity<Map<String, Object>> processHistoricalAttendances(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
                int updatedRecords = shiftAttendanceService.processHistoricalAttendances(fromDate, toDate);
                Map<String, Object> response = Map.of(
                                "success", true,
                                "message", "Đã xử lý điểm danh cho các ca làm việc từ " + fromDate + " đến " + toDate,
                                "updatedRecords", updatedRecords);
                return ResponseEntity.ok(response);
        }
}