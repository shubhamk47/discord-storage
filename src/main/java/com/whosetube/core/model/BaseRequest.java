package com.whosetube.core.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BaseRequest<T> {

    private UUID requestId;
    private LocalDateTime requestTime;
    private T data;

}
