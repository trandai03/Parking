package com.project.parking.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
@Builder
public class  Response {
    private String status;
    private String message;
    private Object data;
}
