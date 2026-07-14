package com.dreamtech.clientresourceaccessapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ClientResourceAccessApiRuntimeException extends RuntimeException {

    public ClientResourceAccessApiRuntimeException(String message) {
        super(message);
    }

}