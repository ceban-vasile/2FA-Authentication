package com.example.demo.exception;

import java.io.Serial;

public class UserNotFoundException extends Exception{
    @Serial
    private static final long serialVersionUID = 1L;

    public UserNotFoundException(String msg) {
        super(msg);
    }
}
