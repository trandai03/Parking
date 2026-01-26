package com.project.parking.response.parkingLot;

import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParkingLotResponse {
    private Long id;
    private String name;
    private String address;
    private int capacity;
    private int availableSlots;
    private String status;
    private List<ParkingSlotResponse> parkingSlots;
}