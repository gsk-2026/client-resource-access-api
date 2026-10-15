package com.dreamtech.clientresourceaccessapi.dto;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record ErrorResponse (

        int status,         // HTTP 400, 404, 409
        String error,
        String path,
        LocalDateTime timestamp

) {}