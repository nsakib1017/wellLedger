package com.subsel.healthledger.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
public class WeakPasswordException extends RuntimeException {
    public WeakPasswordException(String msg)
    {
        super(msg);
    }
}
