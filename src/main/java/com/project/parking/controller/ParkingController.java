package com.project.parking.controller;

import com.project.parking.dto.LicensePlateRecognitionDTO;
import com.project.parking.dto.ParkingSessionDTO;
import com.project.parking.dto.request.ParkingSessionRequest;
import com.project.parking.exceptions.DataNotFoundException;
import com.project.parking.exceptions.InvalidOperationException;
import com.project.parking.service.LicensePlateRecognitionService;
import com.project.parking.service.ParkingService;
import com.project.parking.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Quản lý bãi đỗ xe", description = "APIs để quản lý quá trình ra vào bãi đỗ xe và nhận diện biển số")
public class ParkingController {

        private final ParkingService parkingService;
        private final LicensePlateRecognitionService recognitionService;
        private final PaymentService paymentService;

        @Operation(summary = "Lấy danh sách tất cả phiên gửi xe", description = "API này dùng để lấy danh sách tất cả các phiên gửi xe trong hệ thống.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
        })
        @GetMapping("/sessions")
        public ResponseEntity<List<ParkingSessionDTO>> getAllParkingSessions() {
                log.info("Fetching all parking sessions");
                List<ParkingSessionDTO> sessions = parkingService.getAllParkingSessions();
                return ResponseEntity.ok(sessions);
        }

        @Operation(summary = "Lấy thông tin phiên gửi xe theo ID", description = "API này dùng để lấy thông tin chi tiết của một phiên gửi xe theo ID.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy phiên gửi xe với ID đã cho")
        })
        @GetMapping("/sessions/{id}")
        public ResponseEntity<ParkingSessionDTO> getParkingSessionById(@PathVariable Long id)
                        throws DataNotFoundException {
                log.info("Fetching parking session with id: {}", id);
                ParkingSessionDTO session = parkingService.getParkingSessionById(id);
                return ResponseEntity.ok(session);
        }

        @Operation(summary = "Lấy danh sách phiên gửi xe theo bãi đỗ", description = "API này dùng để lấy danh sách tất cả các phiên gửi xe của một bãi đỗ xe cụ thể.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
        })
        @GetMapping(value = "/sessions/parkingLot/{id}")
        public ResponseEntity<List<ParkingSessionDTO>> getParkingSessionByParkingLot(@PathVariable Long id)
                        throws DataNotFoundException {
                log.info("Fetching parking session with id: {}", id);
                List<ParkingSessionDTO> session = parkingService.getParkingSessionByParkingLot(id);
                return ResponseEntity.ok(session);
        }

        @Operation(summary = "Lấy danh sách phiên gửi xe đang hoạt động", description = "API này dùng để lấy danh sách các phiên gửi xe đang diễn ra (xe đang ở trong bãi).")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
        })
        @GetMapping("/sessions/active")
        public ResponseEntity<List<ParkingSessionDTO>> getActiveParkingSessions() {
                log.info("Fetching all active parking sessions");
                List<ParkingSessionDTO> sessions = parkingService.getActiveParkingSessions();
                return ResponseEntity.ok(sessions);
        }

        @Operation(summary = "Lấy phiên gửi xe đang hoạt động của phương tiện", description = "API này dùng để lấy thông tin phiên gửi xe đang hoạt động của một phương tiện cụ thể.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy phiên gửi xe đang hoạt động cho phương tiện")
        })
        @GetMapping("/sessions/vehicle/{vehicleId}")
        public ResponseEntity<ParkingSessionDTO> getActiveSessionByVehicleId(@PathVariable Long vehicleId)
                        throws DataNotFoundException {
                log.info("Fetching active parking session for vehicle id: {}", vehicleId);
                ParkingSessionDTO session = parkingService.getActiveSessionByVehicleId(vehicleId);
                return ResponseEntity.ok(session);
        }

        @Operation(summary = "Lấy danh sách phiên gửi xe theo biển số", description = "API này dùng để lấy danh sách tất cả các phiên gửi xe của một biển số xe cụ thể.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
        })
        @GetMapping("/sessions/license-plate/{licensePlate}")
        public ResponseEntity<List<ParkingSessionDTO>> getSessionsByLicensePlate(@PathVariable String licensePlate) {
                log.info("Fetching parking sessions for license plate: {}", licensePlate);
                List<ParkingSessionDTO> sessions = parkingService.findSessionsByLicensePlate(licensePlate);
                return ResponseEntity.ok(sessions);
        }

        @Operation(summary = "Đăng ký phương tiện vào bãi đỗ (với thông tin chi tiết)", description = "API này dùng để xử lý khi một phương tiện vào bãi đỗ xe với thông tin chi tiết và hình ảnh biển số.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Đăng ký vào bãi thành công"),
                        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc không nhận diện được biển số"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy bãi đỗ xe với ID đã cho")
        })
        @PostMapping(value = "/entry", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ParkingSessionDTO> createEntrySessionWithDetails(
                        @RequestPart("data") ParkingSessionRequest parkingSessionRequest,
                        @RequestPart("image") MultipartFile image)
                        throws IOException, DataNotFoundException, InvalidOperationException {

                // Sử dụng ParkingService để tạo phiên gửi xe
                ParkingSessionDTO session = parkingService.createEntrySession(parkingSessionRequest, image);
                return new ResponseEntity<>(session, HttpStatus.CREATED);
        }

        @Operation(summary = "Lấy phiên gửi xe theo code", description = "API này dùng để lấy thông tin phiên gửi xe theo code.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy phiên gửi xe với ID đã cho")
        })
        @GetMapping(value = "/sessions/code/{code}")
        public ResponseEntity<ParkingSessionDTO> getParkingSessionByCode(@PathVariable Integer code)
                        throws IOException, DataNotFoundException, InvalidOperationException {

                ParkingSessionDTO session = parkingService.getParkingSessionByCode(code);
                return ResponseEntity.ok(session);
        }

        @Operation(summary = "Xác nhận phiên gửi xe", description = "API này dùng để xác nhận phiên gửi xe đã hoàn thành.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Xác nhận thành công"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy phiên gửi xe với ID đã cho")
        })
        @PostMapping(value = "/exit/{code}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ParkingSessionDTO> confirmExitSession(@PathVariable Integer code,
                        @RequestPart("image") MultipartFile image, @RequestPart("licensePlate") String licensePlate,@RequestPart("paymentMethod") String paymentMethod )
                        throws DataNotFoundException, InvalidOperationException, IOException {
                // Sử dụng ParkingService để hoàn thành phiên gửi xe
                ParkingSessionDTO session = parkingService.completeExitSessionWithRecognition(code, image,
                                licensePlate,paymentMethod);
                return ResponseEntity.ok(session);
        }

        @Operation(summary = "Tính tiền phiên gửi xe", description = "API tính tiền phiên gửi xe. Nếu paymentMethod=CASH trả về số tiền, nếu paymentMethod=MOMO trả về link thanh toán MoMo kèm số tiền.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Tính tiền thành công"),
                        @ApiResponse(responseCode = "400", description = "Phương thức thanh toán không hợp lệ"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy phiên gửi xe với code đã cho")
        })
        @GetMapping(value = "/payment/{code}")
        public ResponseEntity<?> getPayment(
                        @PathVariable Integer code,
                        @RequestParam String paymentMethod)
                        throws DataNotFoundException, InvalidOperationException, IOException {
                BigDecimal totalCost = parkingService.caculateTotalCost(code);
                if ("CASH".equalsIgnoreCase(paymentMethod)) {
                        return ResponseEntity.ok(Map.of(
                                        "paymentMethod", "CASH",
                                        "totalCost", totalCost));
                } else if ("MOMO".equalsIgnoreCase(paymentMethod)) {
                        String paymentUrl = paymentService.createParkingPaymentUrl(totalCost);
                        return ResponseEntity.ok(Map.of(
                                        "paymentMethod", "MOMO",
                                        "totalCost", totalCost,
                                        "paymentUrl", paymentUrl));
                } else {
                        return ResponseEntity.badRequest().body(Map.of(
                                        "error", "Phương thức thanh toán không hợp lệ: " + paymentMethod
                                                        + ". Chỉ hỗ trợ CASH hoặc MOMO."));
                }
        }

        @Operation(summary = "Check-in dành cho thành viên", description = "API này dùng để xử lý khi xe của thành viên vào bãi đỗ xe. Hệ thống sẽ xác minh xe thuộc về thành viên có thẻ đang hoạt động trước khi tạo phiên gửi xe với phí = 0.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Check-in thành viên thành công"),
                        @ApiResponse(responseCode = "400", description = "Xe không thuộc về thành viên hoặc thẻ đã hết hạn"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy xe hoặc bãi đỗ xe")
        })
        @PostMapping(value = "/member/entry", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ParkingSessionDTO> createMemberEntrySession(
                        @RequestPart("data") ParkingSessionRequest parkingSessionRequest,
                        @RequestPart("image") MultipartFile image)
                        throws IOException, DataNotFoundException, InvalidOperationException {
                ParkingSessionDTO session = parkingService.createMemberEntrySession(parkingSessionRequest, image);
                return new ResponseEntity<>(session, HttpStatus.CREATED);
        }

        @Operation(summary = "Check-out dành cho thành viên", description = "API này dùng để xử lý khi xe của thành viên ra khỏi bãi đỗ xe. Phí gửi xe sẽ là 0 vì thành viên đã đóng phí định kỳ.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Check-out thành viên thành công"),
                        @ApiResponse(responseCode = "400", description = "Xe không thuộc về thành viên hoặc biển số không khớp"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy phiên gửi xe với code đã cho")
        })
        @PostMapping(value = "/member/exit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ParkingSessionDTO> confirmMemberExitSession(
                        @RequestPart("data") ParkingSessionRequest parkingSessionRequest,
                        @RequestPart("image") MultipartFile image)
                        throws DataNotFoundException, InvalidOperationException, IOException {
                ParkingSessionDTO session = parkingService.completeMemberExitSession(parkingSessionRequest, image);
                return ResponseEntity.ok(session);
        }

        @Operation(summary = "Nhận diện biển số từ hình ảnh", description = "API này dùng để nhận diện biển số xe từ một hình ảnh được tải lên.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Nhận diện thành công"),
                        @ApiResponse(responseCode = "400", description = "Không thể xử lý hình ảnh hoặc không nhận diện được biển số")
        })
        @PostMapping(value = "/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<?> recognizeLicensePlate(
                        @RequestPart("image") MultipartFile image) throws IOException {
                log.info("License plate recognition request received");
                Map<String, String> result = recognitionService.recognizeLicensePlate(image);
                return ResponseEntity.ok(result);
        }

        @Operation(summary = "Lấy phiên gửi xe theo user", description = "API này dùng để lấy phiên gửi xe theo user.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy phiên gửi xe với ID đã cho")
        })
        @GetMapping(value = "/sessions/user/{userId}")
        public ResponseEntity<ParkingSessionDTO> getParkingSessionByUser(@PathVariable Long userId)
                        throws DataNotFoundException {
                ParkingSessionDTO session = parkingService.getParkingSessionByUser(userId);
                return ResponseEntity.ok(session);
        }

        @Operation(summary = "Lấy phiên gửi xe theo thời gian", description = "API này dùng để lấy phiên gửi xe theo thời gian.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy phiên gửi xe với ID đã cho")
        })
        @GetMapping(value = "/sessions/datetime")
        public ResponseEntity<List<ParkingSessionDTO>> getParkingSessionByDateTime(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateStart,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateEnd)
                        throws DataNotFoundException {
                List<ParkingSessionDTO> sessions = parkingService.getParkingSessionByDateTime(dateStart, dateEnd);
                return ResponseEntity.ok(sessions);
        }
}
