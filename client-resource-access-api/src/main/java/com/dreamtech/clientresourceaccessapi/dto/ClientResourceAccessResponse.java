package com.dreamtech.clientresourceaccessapi.dto;

import lombok.Builder;

@Builder
public record ClientResourceAccessResponse(

        Long clientId,
        Long resourceId,
        String accessCode,
        String description

) {}