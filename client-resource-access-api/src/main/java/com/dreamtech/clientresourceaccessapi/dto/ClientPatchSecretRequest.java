package com.dreamtech.clientresourceaccessapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ClientPatchSecretRequest(

        @NotBlank
        String currentSecret,

        @NotBlank(message = "Client new secret cannot be blank")
        @Size(min = 16, max = 200)
        String newSecret

) {}
