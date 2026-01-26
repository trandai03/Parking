package com.project.parking.response.parkingLot;

import lombok.*;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParkingSlotResponse {
    private Long id;
    private String slotNumber;
    private String status;
    private String type;
}