package com.dreamtech.clientresourceaccessapi.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ResourceResponse(

        Long id,
        String key,
        String type,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}
