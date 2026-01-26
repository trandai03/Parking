package com.project.parking.controller;

import com.project.parking.dto.ParkingLotDTO;
import com.project.parking.dto.request.EmployeeRequest;
import com.project.parking.dto.request.ParkingLotRequest;
import com.project.parking.exceptions.DataNotFoundException;
import com.project.parking.service.ParkingLotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parking-lots")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Quản lý bãi đỗ xe", description = "APIs để quản lý thông tin các bãi đỗ xe trong hệ thống")
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    @Operation(summary = "Lấy danh sách bãi đỗ xe", description = "API này dùng để lấy danh sách bãi đỗ xe với các bộ lọc tùy chọn như tên, loại xe, có mái che và trạng thái.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    @GetMapping
    public ResponseEntity<List<ParkingLotDTO>> getAllParkingLots(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<String> vehicleTypes,
            @RequestParam(required = false) Boolean isCovered,
            @RequestParam(required = false) String status) {
        log.info("Fetching parking lots with filters - name: {}, vehicleTypes: {}, isCovered: {}, status: {}",
                name, vehicleTypes, isCovered, status);
        List<ParkingLotDTO> parkingLots = parkingLotService.getAllParkingLots(name, vehicleTypes, isCovered, status);
        return ResponseEntity.ok(parkingLots);
    }

    @Operation(summary = "Lấy thông tin bãi đỗ xe theo ID", description = "API này dùng để lấy thông tin chi tiết của một bãi đỗ xe theo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bãi đỗ xe với ID đã cho")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ParkingLotDTO> getParkingLotById(@PathVariable Long id) throws DataNotFoundException {
        log.info("Fetching parking lot with id: {}", id);
        ParkingLotDTO parkingLot = parkingLotService.getParkingLotById(id);
        return ResponseEntity.ok(parkingLot);
    }

    @Operation(summary = "Lấy danh sách bãi đỗ xe theo trạng thái", description = "API này dùng để lấy danh sách các bãi đỗ xe theo trạng thái (ACTIVE, INACTIVE, v.v.).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ParkingLotDTO>> getParkingLotsByStatus(@PathVariable String status) {
        log.info("Fetching parking lots with status: {}", status);
        List<ParkingLotDTO> parkingLots = parkingLotService.getAllParkingLots(null, null, null, status);
        return ResponseEntity.ok(parkingLots);
    }

    @Operation(summary = "Tạo bãi đỗ xe mới", description = "API này dùng để đăng ký bãi đỗ xe mới vào hệ thống.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo bãi đỗ xe thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chủ sở hữu bãi đỗ xe")
    })
    @PostMapping
    public ResponseEntity<ParkingLotDTO> createParkingLot(@RequestBody ParkingLotRequest parkingLotRequest)
            throws DataNotFoundException {
        log.info("Creating new parking lot: {}", parkingLotRequest.getName());
        ParkingLotDTO createdParkingLot = parkingLotService.createParkingLot(parkingLotRequest);
        return new ResponseEntity<>(createdParkingLot, HttpStatus.CREATED);
    }

    @Operation(summary = "Cập nhật thông tin bãi đỗ xe", description = "API này dùng để cập nhật thông tin của một bãi đỗ xe đã đăng ký.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bãi đỗ xe với ID đã cho")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ParkingLotDTO> updateParkingLot(@PathVariable Long id,
            @RequestBody ParkingLotDTO parkingLotDTO) throws DataNotFoundException {
        log.info("Updating parking lot with id: {}", id);
        ParkingLotDTO updatedParkingLot = parkingLotService.updateParkingLot(id, parkingLotDTO);
        return ResponseEntity.ok(updatedParkingLot);
    }

    @Operation(summary = "Xóa bãi đỗ xe", description = "API này dùng để xóa bãi đỗ xe khỏi hệ thống.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bãi đỗ xe với ID đã cho")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParkingLot(@PathVariable Long id) throws DataNotFoundException {
        log.info("Deleting parking lot with id: {}", id);
        parkingLotService.deleteParkingLot(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cập nhật số lượng chỗ trống", description = "API này dùng để cập nhật số lượng chỗ trống trong bãi đỗ xe, có thể tăng hoặc giảm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Không đủ chỗ trống hoặc vượt quá sức chứa"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bãi đỗ xe với ID đã cho")
    })
    @PatchMapping("/{id}/availability")
    public ResponseEntity<ParkingLotDTO> updateParkingLotAvailability(
            @PathVariable Long id,
            @RequestParam Integer spotsChange) throws DataNotFoundException {
        log.info("Updating parking lot availability for id: {}, change: {}", id, spotsChange);
        ParkingLotDTO updatedParkingLot = parkingLotService.updateParkingLotAvailability(id, spotsChange);
        return ResponseEntity.ok(updatedParkingLot);
    }
    @Operation(summary = "Thêm nhân viên vào bãi đỗ xe", description = "API này dùng để thêm nhân viên vào bãi đỗ xe.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thêm nhân viên thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bãi đỗ xe với ID đã cho")
    })
    @PostMapping("/employees")
    public ResponseEntity<ParkingLotDTO> addEmployeeToParkingLot(
        @RequestBody EmployeeRequest employeeRequest
    ) throws Exception {
        ParkingLotDTO updatedParkingLot = parkingLotService.addEmployeeToParkingLot( employeeRequest);
        return ResponseEntity.ok(updatedParkingLot);
    }
}