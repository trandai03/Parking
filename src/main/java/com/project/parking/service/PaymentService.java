//package com.project.parking.service;
//
//import com.project.parking.dto.PaymentDTO;
//import com.project.parking.exceptions.DataNotFoundException;
//import com.project.parking.model.ParkingSession;
//import com.project.parking.model.Payment;
//import com.project.parking.repository.ParkingSessionRepository;
//import com.project.parking.repository.PaymentRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class PaymentService {
//
//    private final PaymentRepository paymentRepository;
//    private final ParkingSessionRepository parkingSessionRepository;
//
//    public List<PaymentDTO> getAllPayments() {
//        return paymentRepository.findAll().stream()
//                .map(this::convertToDTO)
//                .collect(Collectors.toList());
//    }
//
//    public PaymentDTO getPaymentById(Long id) {
//        Payment payment = paymentRepository.findById(id)
//                .orElseThrow(() -> new DataNotFoundException("Payment not found with id: " + id));
//        return convertToDTO(payment);
//    }
//
//    public List<PaymentDTO> getPaymentsBySessionId(Long sessionId) {
//        return paymentRepository.findBySessionId(sessionId).stream()
//                .map(this::convertToDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional
//    public PaymentDTO createPayment(PaymentDTO paymentDTO) {
//        // Lấy thông tin phiên gửi xe
//        ParkingSession parkingSession = parkingSessionRepository.findById(paymentDTO.getSessionId())
//                .orElseThrow(() -> new DataNotFoundException("Parking session not found with id: " + paymentDTO.getSessionId()));
//
//        // Tạo thanh toán mới
//        Payment payment = new Payment();
//        payment.setSession(parkingSession);
//        payment.setAmount(paymentDTO.getAmount());
//        payment.setPaymentMethod(paymentDTO.getPaymentMethod());
//        payment.setTransactionId(paymentDTO.getTransactionId() != null ?
//                paymentDTO.getTransactionId() : generateTransactionId());
//        payment.setStatus("PENDING"); // Mặc định trạng thái là PENDING
//        payment.setPaymentTime(LocalDateTime.now());
//        payment.setCreatedAt(LocalDateTime.now());
//        payment.setUpdatedAt(LocalDateTime.now());
//
//        Payment savedPayment = paymentRepository.save(payment);
//        return convertToDTO(savedPayment);
//    }
//
//    @Transactional
//    public PaymentDTO updatePaymentStatus(Long id, String status) throws DataNotFoundException {
//        Payment payment = paymentRepository.findById(id)
//                .orElseThrow(() -> new DataNotFoundException("Payment not found with id: " + id));
//
//        payment.setStatus(status);
//        payment.setUpdatedAt(LocalDateTime.now());
//
//        if (status.equals("COMPLETED")) {
//            payment.setPaymentTime(LocalDateTime.now());
//
//            // Cập nhật trạng thái phiên gửi xe nếu thanh toán hoàn tất
//            ParkingSession session = payment.getSession();
//            if (session != null && session.getStatus().equals("ACTIVE")) {
//                session.setStatus("PAID");
//                parkingSessionRepository.save(session);
//            }
//        }
//
//        Payment updatedPayment = paymentRepository.save(payment);
//        return convertToDTO(updatedPayment);
//    }
//
//    public void deletePayment(Long id) {
//        if (!paymentRepository.existsById(id)) {
//            throw new DataNotFoundException("Payment not found with id: " + id);
//        }
//        paymentRepository.deleteById(id);
//    }
//
//    private PaymentDTO convertToDTO(Payment payment) {
//        return PaymentDTO.builder()
//                .id(payment.getId())
//                .sessionId(payment.getSession().getId())
//                .amount(payment.getAmount())
//                .paymentMethod(payment.getPaymentMethod())
//                .transactionId(payment.getTransactionId())
//                .status(payment.getStatus())
//                .paymentTime(payment.getPaymentTime())
//                .build();
//    }
//
//    private String generateTransactionId() {
//        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
//    }
//}