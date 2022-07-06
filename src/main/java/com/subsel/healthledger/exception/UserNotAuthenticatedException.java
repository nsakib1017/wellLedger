package com.subsel.healthledger.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UserNotAuthenticatedException extends RuntimeException
{
    public UserNotAuthenticatedException()
    {
        super(String.format("User not authenticated to perform this operation"));
    }
}
