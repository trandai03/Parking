package com.project.parking.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseList<T> {
    private T data;
    private String message;
}
