package com.dreamtech.clientresourceaccessapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ClientPutRequest(

        @NotBlank(message = "Client key cannot be blank")
        @Size(min = 3, max = 100, message = "Client key must be between 3 and 100 characters")
        String key,

        String description

) {}
