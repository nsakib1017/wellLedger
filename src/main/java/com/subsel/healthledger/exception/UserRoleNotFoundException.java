package com.subsel.healthledger.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class UserRoleNotFoundException extends ResourceNotFoundException
{
    public UserRoleNotFoundException(Object fieldValue)
    {
        this("unique id", fieldValue);
    }

    public UserRoleNotFoundException(String fieldName, Object fieldValue)
    {
        super("UserRole", fieldName, fieldValue);
    }
}
