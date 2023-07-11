package com.wishquill.userservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserAlreadyExistsException extends ResponseStatusException {
    public UserAlreadyExistsException(String msg, Object... objects) {
        super(HttpStatus.CONFLICT, String.format(msg, objects));
    }

}
