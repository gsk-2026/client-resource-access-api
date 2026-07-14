package com.dreamtech.clientresourceaccessapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ClientResourceAccessPostRequest(

        @NotNull(message = "client id cannot be blank")
        Long clientId,

        @NotNull(message = "resource id cannot be blank")
        Long resourceId,

        @NotBlank(message = "client access code for a resource cannot be blank")
        @Size(min = 3, max = 100, message = "Client access code must be between 3 and 100 characters")
        String accessCode,

        String description

) {}