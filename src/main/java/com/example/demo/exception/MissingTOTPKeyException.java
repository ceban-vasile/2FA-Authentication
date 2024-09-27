package com.example.demo.exception;

import org.springframework.security.core.AuthenticationException;

import java.io.Serial;

public class MissingTOTPKeyException extends AuthenticationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public MissingTOTPKeyException(String msg) {
        super(msg);
    }
}
