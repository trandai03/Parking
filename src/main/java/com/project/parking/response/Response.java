package com.project.parking.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class  Response {
    private String status;
    private String message;
    private Object data;
}
