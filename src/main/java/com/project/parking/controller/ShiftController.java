package com.project.parking.controller;

import com.project.parking.dto.ShiftDTO;
import com.project.parking.service.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
@Tag(name = "Shift Controller", description = "Quản lý ca làm việc")
public class ShiftController {

    private final ShiftService shiftService;

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả ca làm việc", description = "API này dùng để lấy danh sách tất cả ca làm việc trong hệ thống.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<List<ShiftDTO>> getAllShifts() {
        return ResponseEntity.ok(shiftService.getAllShifts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin ca làm việc theo ID", description = "API này dùng để lấy thông tin chi tiết của một ca làm việc dựa vào ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy ca làm việc")
    })
    public ResponseEntity<ShiftDTO> getShiftById(@PathVariable Long id) {
        return ResponseEntity.ok(shiftService.getShiftById(id));
    }

    @PostMapping
    @Operation(summary = "Tạo mới ca làm việc", description = "API này dùng để tạo mới một ca làm việc trong hệ thống.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo mới thành công")
    })
    public ResponseEntity<ShiftDTO> createShift(@RequestBody ShiftDTO shiftDTO) {
        return new ResponseEntity<>(shiftService.createShift(shiftDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật ca làm việc", description = "API này dùng để cập nhật thông tin của ca làm việc dựa vào ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy ca làm việc")
    })
    public ResponseEntity<ShiftDTO> updateShift(@PathVariable Long id, @RequestBody ShiftDTO shiftDTO) {
        return ResponseEntity.ok(shiftService.updateShift(id, shiftDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa ca làm việc", description = "API này dùng để xóa một ca làm việc khỏi hệ thống dựa vào ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy ca làm việc")
    })
    public ResponseEntity<Void> deleteShift(@PathVariable Long id) {
        shiftService.deleteShift(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active")
    @Operation(summary = "Lấy danh sách ca làm việc đang hoạt động", description = "API này dùng để lấy danh sách tất cả ca làm việc có trạng thái ACTIVE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<List<ShiftDTO>> getActiveShifts() {
        return ResponseEntity.ok(shiftService.getActiveShifts());
    }

    @GetMapping("/name/{shiftName}")
    @Operation(summary = "Tìm ca làm việc theo tên", description = "API này dùng để tìm kiếm ca làm việc dựa vào tên.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm kiếm thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy ca làm việc với tên đã cho")
    })
    public ResponseEntity<ShiftDTO> getShiftByName(@PathVariable String shiftName) {
        return ResponseEntity.ok(shiftService.getShiftByName(shiftName));
    }
}