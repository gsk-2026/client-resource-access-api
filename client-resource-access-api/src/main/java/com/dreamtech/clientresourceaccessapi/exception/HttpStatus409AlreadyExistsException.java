package com.dreamtech.clientresourceaccessapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.CONFLICT) // This automatically maps to HTTP 409
public class HttpStatus409AlreadyExistsException extends RuntimeException {
    public HttpStatus409AlreadyExistsException(String message) {
        super(message);
    }
}