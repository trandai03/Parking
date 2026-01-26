package com.project.parking.response.payment;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long parkingSessionId;
    private String paymentMethod;
    private BigDecimal amount;
    private String status;
    private LocalDateTime paymentTime;
    private String transactionId;
}