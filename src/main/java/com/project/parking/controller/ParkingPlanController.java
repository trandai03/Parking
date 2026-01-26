package com.project.parking.controller;

import com.project.parking.dto.ParkingPlanDTO;
import com.project.parking.dto.request.CreateParkingPlanRequest;
import com.project.parking.dto.request.UpdateParkingPlanRequest;
import com.project.parking.response.Response;
import com.project.parking.service.ParkingPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.v1.prefix:/api/v1}/parking-plans")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Parking Plan Management", description = "APIs quản lý các gói gửi xe (CRUD)")
public class ParkingPlanController {

    private final ParkingPlanService parkingPlanService;

    // ============ PUBLIC ENDPOINTS ============

    @Operation(summary = "Lấy danh sách gói theo bãi đỗ xe", 
               description = "API này dùng để lấy danh sách các gói gửi xe đang hoạt động của một bãi đỗ xe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    @GetMapping("/parking-lot/{parkingLotId}")
    public ResponseEntity<Response> getPlansByParkingLot(
            @Parameter(description = "ID của bãi đỗ xe") @PathVariable Long parkingLotId) {
        try {
            log.info("Getting plans for parking lot: {}", parkingLotId);
            List<ParkingPlanDTO> plans = parkingPlanService.getPlansByParkingLot(parkingLotId);
            return ResponseEntity.ok(new Response("success", "Lấy danh sách gói thành công", plans));
        } catch (Exception e) {
            log.error("Error getting plans for parking lot: {}", parkingLotId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Lấy thông tin gói theo ID", 
               description = "API này dùng để lấy thông tin chi tiết của một gói gửi xe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy gói")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Response> getPlanById(
            @Parameter(description = "ID của gói") @PathVariable Long id) {
        try {
            log.info("Getting plan with id: {}", id);
            ParkingPlanDTO plan = parkingPlanService.getPlanById(id);
            return ResponseEntity.ok(new Response("success", "Lấy thông tin gói thành công", plan));
        } catch (Exception e) {
            log.error("Error getting plan with id: {}", id, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Lấy danh sách gói phổ biến", 
               description = "API này dùng để lấy danh sách các gói được đánh dấu là phổ biến")
    @GetMapping("/popular")
    public ResponseEntity<Response> getPopularPlans() {
        try {
            log.info("Getting popular plans");
            List<ParkingPlanDTO> plans = parkingPlanService.getPopularPlans();
            return ResponseEntity.ok(new Response("success", "Lấy danh sách gói phổ biến thành công", plans));
        } catch (Exception e) {
            log.error("Error getting popular plans", e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    // ============ OWNER/ADMIN ENDPOINTS ============

    @Operation(summary = "Lấy tất cả gói theo bãi đỗ xe (bao gồm inactive)", 
               description = "API này dành cho Owner/Admin để quản lý tất cả các gói")
    @GetMapping("/parking-lot/{parkingLotId}/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> getAllPlansByParkingLot(
            @Parameter(description = "ID của bãi đỗ xe") @PathVariable Long parkingLotId) {
        try {
            log.info("Getting all plans for parking lot: {}", parkingLotId);
            List<ParkingPlanDTO> plans = parkingPlanService.getAllPlansByParkingLot(parkingLotId);
            return ResponseEntity.ok(new Response("success", "Lấy danh sách gói thành công", plans));
        } catch (Exception e) {
            log.error("Error getting all plans for parking lot: {}", parkingLotId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Tạo gói mới", 
               description = "API này dùng để tạo một gói gửi xe mới cho bãi đỗ xe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo gói thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Bãi đỗ xe không tồn tại")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> createPlan(@RequestBody @Valid CreateParkingPlanRequest request) {
        try {
            log.info("Creating new parking plan: {}", request.getName());
            ParkingPlanDTO plan = parkingPlanService.createPlan(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new Response("success", "Tạo gói thành công", plan));
        } catch (Exception e) {
            log.error("Error creating parking plan", e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Cập nhật gói", 
               description = "API này dùng để cập nhật thông tin của một gói gửi xe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy gói")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> updatePlan(
            @Parameter(description = "ID của gói") @PathVariable Long id,
            @RequestBody @Valid UpdateParkingPlanRequest request) {
        try {
            log.info("Updating parking plan: {}", id);
            ParkingPlanDTO plan = parkingPlanService.updatePlan(id, request);
            return ResponseEntity.ok(new Response("success", "Cập nhật gói thành công", plan));
        } catch (Exception e) {
            log.error("Error updating parking plan: {}", id, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Vô hiệu hóa gói (soft delete)", 
               description = "API này dùng để vô hiệu hóa một gói gửi xe (không xóa khỏi database)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vô hiệu hóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy gói")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> deletePlan(
            @Parameter(description = "ID của gói") @PathVariable Long id) {
        try {
            log.info("Deleting parking plan: {}", id);
            parkingPlanService.deletePlan(id);
            return ResponseEntity.ok(new Response("success", "Vô hiệu hóa gói thành công", null));
        } catch (Exception e) {
            log.error("Error deleting parking plan: {}", id, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Xóa vĩnh viễn gói", 
               description = "API này dùng để xóa vĩnh viễn một gói khỏi database (chỉ Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy gói")
    })
    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> hardDeletePlan(
            @Parameter(description = "ID của gói") @PathVariable Long id) {
        try {
            log.info("Hard deleting parking plan: {}", id);
            parkingPlanService.hardDeletePlan(id);
            return ResponseEntity.ok(new Response("success", "Xóa vĩnh viễn gói thành công", null));
        } catch (Exception e) {
            log.error("Error hard deleting parking plan: {}", id, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }
}

