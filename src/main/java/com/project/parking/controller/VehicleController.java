package com.project.parking.controller;

import com.project.parking.dto.VehicleDTO;
import com.project.parking.exceptions.DataNotFoundException;
import com.project.parking.service.VehicleService;
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
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Quản lý phương tiện", description = "APIs để quản lý thông tin các phương tiện giao thông")
public class VehicleController {

    private final VehicleService vehicleService;

    @Operation(summary = "Lấy danh sách tất cả phương tiện", description = "API này dùng để lấy danh sách tất cả các phương tiện đã đăng ký trong hệ thống.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    @GetMapping
    public ResponseEntity<List<VehicleDTO>> getAllVehicles() {
        log.info("Fetching all vehicles");
        List<VehicleDTO> vehicles = vehicleService.getAllVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @Operation(summary = "Lấy thông tin phương tiện theo ID", description = "API này dùng để lấy thông tin chi tiết của một phương tiện theo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy phương tiện với ID đã cho")
    })
    @GetMapping("/{id}")
    public ResponseEntity<VehicleDTO> getVehicleById(@PathVariable Long id) throws DataNotFoundException {
        log.info("Fetching vehicle with id: {}", id);
        VehicleDTO vehicle = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(vehicle);
    }

    @Operation(summary = "Lấy thông tin phương tiện theo biển số", description = "API này dùng để lấy thông tin chi tiết của một phương tiện theo biển số xe.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy phương tiện với biển số đã cho")
    })
    @GetMapping("/license-plate/{licensePlate}")
    public ResponseEntity<VehicleDTO> getVehicleByLicensePlate(@PathVariable String licensePlate)
            throws DataNotFoundException {
        log.info("Fetching vehicle with license plate: {}", licensePlate);
        VehicleDTO vehicle = vehicleService.getVehicleByLicensePlate(licensePlate);
        return ResponseEntity.ok(vehicle);
    }

    @Operation(summary = "Tạo phương tiện mới", description = "API này dùng để đăng ký phương tiện mới vào hệ thống.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo phương tiện thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc biển số đã tồn tại")
    })
    @PostMapping
    public ResponseEntity<VehicleDTO> createVehicle(@RequestBody VehicleDTO vehicleDTO) throws DataNotFoundException {
        log.info("Creating new vehicle with license plate: {}", vehicleDTO.getLicensePlate());
        VehicleDTO createdVehicle = vehicleService.createVehicle(vehicleDTO);
        return new ResponseEntity<>(createdVehicle, HttpStatus.CREATED);
    }

    @Operation(summary = "Cập nhật thông tin phương tiện", description = "API này dùng để cập nhật thông tin của một phương tiện đã đăng ký.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy phương tiện với ID đã cho")
    })
    @PutMapping("/{id}")
    public ResponseEntity<VehicleDTO> updateVehicle(@PathVariable Long id, @RequestBody VehicleDTO vehicleDTO)
            throws DataNotFoundException {
        log.info("Updating vehicle with id: {}", id);
        VehicleDTO updatedVehicle = vehicleService.updateVehicle(id, vehicleDTO);
        return ResponseEntity.ok(updatedVehicle);
    }

    @Operation(summary = "Xóa phương tiện", description = "API này dùng để xóa phương tiện khỏi hệ thống.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy phương tiện với ID đã cho")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) throws DataNotFoundException {
        log.info("Deleting vehicle with id: {}", id);
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}