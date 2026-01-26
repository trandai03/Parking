// package com.project.parking.controller;

// import com.project.parking.dto.PaymentDTO;
// import com.project.parking.dto.PaymentStatusUpdateDTO;
// import com.project.parking.exceptions.DataNotFoundException;
// import com.project.parking.service.PaymentService;
// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.responses.ApiResponse;
// import io.swagger.v3.oas.annotations.responses.ApiResponses;
// import io.swagger.v3.oas.annotations.tags.Tag;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;

// @RestController
// @RequestMapping("/api/payments")
// @RequiredArgsConstructor
// @Slf4j
// @Tag(name = "Quản lý thanh toán", description = "APIs để quản lý các giao dịch thanh toán trong hệ thống")
// public class PaymentController {

//     private final PaymentService paymentService;

//     @Operation(summary = "Lấy danh sách tất cả thanh toán", description = "API này dùng để lấy danh sách tất cả các giao dịch thanh toán trong hệ thống.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
//     })
//     @GetMapping
//     public ResponseEntity<List<PaymentDTO>> getAllPayments() {
//         log.info("Fetching all payments");
//         List<PaymentDTO> payments = paymentService.getAllPayments();
//         return ResponseEntity.ok(payments);
//     }

//     @Operation(summary = "Lấy thông tin thanh toán theo ID", description = "API này dùng để lấy thông tin chi tiết của một giao dịch thanh toán theo ID.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
//             @ApiResponse(responseCode = "404", description = "Không tìm thấy thanh toán với ID đã cho")
//     })
//     @GetMapping("/{id}")
//     public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable Long id) throws DataNotFoundException {
//         log.info("Fetching payment with id: {}", id);
//         PaymentDTO payment = paymentService.getPaymentById(id);
//         return ResponseEntity.ok(payment);
//     }

//     @Operation(summary = "Lấy danh sách thanh toán theo phiên gửi xe", description = "API này dùng để lấy danh sách các giao dịch thanh toán liên quan đến một phiên gửi xe cụ thể.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
//     })
//     @GetMapping("/session/{sessionId}")
//     public ResponseEntity<List<PaymentDTO>> getPaymentsBySessionId(@PathVariable Long sessionId) {
//         log.info("Fetching payments for session id: {}", sessionId);
//         List<PaymentDTO> payments = paymentService.getPaymentsBySessionId(sessionId);
//         return ResponseEntity.ok(payments);
//     }

//     @Operation(summary = "Tạo thanh toán mới", description = "API này dùng để tạo một giao dịch thanh toán mới trong hệ thống.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "201", description = "Tạo thanh toán thành công"),
//             @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
//             @ApiResponse(responseCode = "404", description = "Không tìm thấy phiên gửi xe liên quan")
//     })
//     @PostMapping
//     public ResponseEntity<PaymentDTO> createPayment(@RequestBody PaymentDTO paymentDTO) throws DataNotFoundException {
//         log.info("Creating new payment for session id: {}", paymentDTO.getSessionId());
//         PaymentDTO createdPayment = paymentService.createPayment(paymentDTO);
//         return new ResponseEntity<>(createdPayment, HttpStatus.CREATED);
//     }

//     @Operation(summary = "Cập nhật trạng thái thanh toán", description = "API này dùng để cập nhật trạng thái của một giao dịch thanh toán (PENDING, COMPLETED, FAILED).")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "200", description = "Cập nhật trạng thái thành công"),
//             @ApiResponse(responseCode = "400", description = "Trạng thái không hợp lệ"),
//             @ApiResponse(responseCode = "404", description = "Không tìm thấy thanh toán với ID đã cho")
//     })
//     @PatchMapping("/{id}/status")
//     public ResponseEntity<PaymentDTO> updatePaymentStatus(
//             @PathVariable Long id,
//             @RequestBody PaymentStatusUpdateDTO statusUpdateDTO) throws DataNotFoundException {
//         log.info("Updating payment status for id: {} to {}", id, statusUpdateDTO.getStatus());
//         PaymentDTO updatedPayment = paymentService.updatePaymentStatus(id, statusUpdateDTO.getStatus());
//         return ResponseEntity.ok(updatedPayment);
//     }

//     @Operation(summary = "Xóa thanh toán", description = "API này dùng để xóa một giao dịch thanh toán khỏi hệ thống.")
//     @ApiResponses(value = {
//             @ApiResponse(responseCode = "204", description = "Xóa thanh toán thành công"),
//             @ApiResponse(responseCode = "404", description = "Không tìm thấy thanh toán với ID đã cho")
//     })
//     @DeleteMapping("/{id}")
//     public ResponseEntity<Void> deletePayment(@PathVariable Long id) throws DataNotFoundException {
//         log.info("Deleting payment with id: {}", id);
//         paymentService.deletePayment(id);
//         return ResponseEntity.noContent().build();
//     }
// }