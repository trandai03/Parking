package com.project.parking.controller;

import com.project.parking.dto.RevenueStatDTO;
import com.project.parking.exceptions.DataNotFoundException;
import com.project.parking.service.RevenueStatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/revenue-stats")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Thống kê doanh thu", description = "APIs để quản lý và truy vấn thống kê doanh thu của bãi đỗ xe")
public class RevenueStatController {

    private final RevenueStatService revenueStatService;

    @Operation(summary = "Lấy tất cả thống kê doanh thu", description = "API này dùng để lấy danh sách tất cả các bản ghi thống kê doanh thu trong hệ thống.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    @GetMapping
    public ResponseEntity<List<RevenueStatDTO>> getAllRevenueStats(@RequestParam(required = false) Long parkingLotId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Fetching all revenue statistics");
        List<RevenueStatDTO> stats = revenueStatService.getAllRevenueStats(parkingLotId, startDate, endDate);
        return ResponseEntity.ok(stats);
    }

//    @Operation(summary = "Lấy thống kê doanh thu theo ID", description = "API này dùng để lấy thông tin chi tiết của một bản ghi thống kê doanh thu theo ID.")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
//            @ApiResponse(responseCode = "404", description = "Không tìm thấy bản ghi thống kê doanh thu với ID đã cho")
//    })
//     @GetMapping("/{id}")
//     public ResponseEntity<RevenueStatDTO> getRevenueStatById(@PathVariable Long id) throws DataNotFoundException {
//         log.info("Fetching revenue statistics with id: {}", id);
//         RevenueStatDTO stat = revenueStatService.getRevenueStatById(id);
//         return ResponseEntity.ok(stat);
//     }

//     @Operation(summary = "Lấy thống kê doanh thu theo bãi đỗ xe", description = "API này dùng để lấy danh sách các bản ghi thống kê doanh thu của một bãi đỗ xe cụ thể.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
//     })
//     @GetMapping("/parking-lot/{parkingLotId}")
//     public ResponseEntity<List<RevenueStatDTO>> getRevenueStatsByParkingLot(@PathVariable Long parkingLotId) {
//         log.info("Fetching revenue statistics for parking lot id: {}", parkingLotId);
//         List<RevenueStatDTO> stats = revenueStatService.getRevenueStatsByParkingLot(parkingLotId);
//         return ResponseEntity.ok(stats);
//     }

//     @Operation(summary = "Lấy thống kê doanh thu theo khoảng thời gian", description = "API này dùng để lấy danh sách các bản ghi thống kê doanh thu trong một khoảng thời gian cụ thể.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
//     })
//     @GetMapping("/date-range")
//     public ResponseEntity<List<RevenueStatDTO>> getRevenueStatsByDateRange(
//             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
//         log.info("Fetching revenue statistics between {} and {}", startDate, endDate);
//         List<RevenueStatDTO> stats = revenueStatService.getRevenueStatsByDateRange(startDate, endDate);
//         return ResponseEntity.ok(stats);
//     }

//     @Operation(summary = "Lấy thống kê doanh thu theo bãi đỗ xe và khoảng thời gian", description = "API này dùng để lấy danh sách các bản ghi thống kê doanh thu của một bãi đỗ xe cụ thể trong một khoảng thời gian.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
//     })
//     @GetMapping("/parking-lot/{parkingLotId}/date-range")
//     public ResponseEntity<List<RevenueStatDTO>> getRevenueStatsByParkingLotAndDateRange(
//             @PathVariable Long parkingLotId,
//             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
//             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
//         log.info("Fetching revenue statistics for parking lot id: {} between {} and {}",
//                 parkingLotId, startDate, endDate);
//         List<RevenueStatDTO> stats = revenueStatService.getRevenueStatsByParkingLotAndDateRange(
//                 parkingLotId, startDate, endDate);
//         return ResponseEntity.ok(stats);
//     }

    @Operation(summary = "Tạo thống kê doanh thu cho một ngày cụ thể", description = "API này dùng để tạo thủ công các bản ghi thống kê doanh thu cho một ngày cụ thể.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo thống kê thành công")
    })
    @PostMapping("/generate/{date}")
    public ResponseEntity<Void> generateRevenueStatsForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Manually generating revenue statistics for date: {}", date);
        revenueStatService.generateRevenueStatsForDate(date);
        return ResponseEntity.ok().build();
    }
}