package com.whosetube.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class BaseResponse<T> {

    private UUID requestId;
    private LocalDateTime requestTime;
    private LocalDateTime responseTime;
    private T data;

    public BaseResponse(LocalDateTime responseTime, T data){
        this.responseTime = responseTime;
        this.data = data;
    }

}
