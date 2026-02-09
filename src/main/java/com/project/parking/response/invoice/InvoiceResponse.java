package com.project.parking.response.invoice;

import com.project.parking.enums.InvoiceStatus;
import com.project.parking.enums.InvoiceType;
import com.project.parking.model.Invoice;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Getter
@Setter
@Builder
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceResponse {
    private Long id;
    private String invoiceCode;
    private InvoiceType type;
    private BigDecimal amount;
    private InvoiceStatus status;
    private LocalDateTime paymentDeadline;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private Long memberId;
    private Long sessionId;
    private Long parkingPlanId;

    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceCode(),
                invoice.getType(),
                invoice.getAmount(),
                invoice.getStatus(),
                invoice.getPaymentDeadline(),
                invoice.getCreatedAt(),
                invoice.getPaidAt(),
                invoice.getMemberId(),
                invoice.getSessionId(),
                invoice.getParkingPlanId());
    }

    public static List<InvoiceResponse> from(List<Invoice> invoices) {
        return invoices.stream().map(InvoiceResponse::from).collect(Collectors.toList());
    }
}
