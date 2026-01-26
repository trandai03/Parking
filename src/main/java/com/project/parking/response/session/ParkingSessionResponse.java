package com.project.parking.response.session;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParkingSessionResponse {
    private Long id;
    private String vehicleLicensePlate;
    private String parkingLotName;
    private String slotNumber;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private BigDecimal fee;
    private String status;
}