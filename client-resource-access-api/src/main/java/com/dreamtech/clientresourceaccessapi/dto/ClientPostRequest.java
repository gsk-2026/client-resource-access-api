package com.dreamtech.clientresourceaccessapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ClientPostRequest(

    @NotBlank(message = "Client key cannot be blank")
    @Size(min = 3, max = 100, message = "Client key must be between 3 and 100 characters")
    String key,

    // Please not this is secret, NOT secretHash as client never sends hash, the server hashes it
    //@NotBlank(message = "Client secret cannot be blank")
    @Size(min = 16, max = 128, message = "Client secret must be between 16 and 128 characters")
    String secret,

    String description

) {}
