package com.dreamtech.clientresourceaccessapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class HttpStatus404NotFoundException extends RuntimeException {

    public HttpStatus404NotFoundException(String message) {
        super(message);
    }

}