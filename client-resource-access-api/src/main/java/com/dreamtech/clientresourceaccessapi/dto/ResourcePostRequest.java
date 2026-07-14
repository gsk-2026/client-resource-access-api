package com.dreamtech.clientresourceaccessapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;


@Builder
public record ResourcePostRequest(

        @NotBlank(message = "key cannot be blank")
        @Size(min = 3, max = 128, message = "key must be between 3 and 128 characters")
        String key,

        @NotBlank(message = "type cannot be blank")
        @Size(min = 5, max = 100, message = "type must be between 5 and 100 characters")
        String type,

        @Size(max = 500, message = "description cannot exceed 500 characters")
        String description

) {}
