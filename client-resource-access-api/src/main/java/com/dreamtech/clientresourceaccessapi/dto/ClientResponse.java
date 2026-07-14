package com.dreamtech.clientresourceaccessapi.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ClientResponse(

        Long id,
        String key,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}