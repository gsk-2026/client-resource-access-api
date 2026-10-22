package com.dreamtech.clientresourceaccessapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
public class HttpStatus422UnprocessableException extends RuntimeException {
    public HttpStatus422UnprocessableException(String message) {
        super(message);
    }
}
