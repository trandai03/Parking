package com.project.parking.response.vehicle;

import lombok.*;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleResponse {
    private Long id;
    private String licensePlate;
    private String type;
    private String color;
    private Long userId;
}