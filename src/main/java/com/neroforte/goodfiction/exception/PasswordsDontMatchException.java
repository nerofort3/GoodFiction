package com.neroforte.goodfiction.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.FORBIDDEN)
public class PasswordsDontMatchException extends RuntimeException {

    public PasswordsDontMatchException(String message) {
        super(message);
    }
}
