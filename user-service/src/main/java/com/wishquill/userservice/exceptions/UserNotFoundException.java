package com.wishquill.userservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserNotFoundException extends ResponseStatusException {
    public UserNotFoundException(String msg, Object... objects) {
        super(HttpStatus.NOT_FOUND, String.format(msg, objects));
    }
}
