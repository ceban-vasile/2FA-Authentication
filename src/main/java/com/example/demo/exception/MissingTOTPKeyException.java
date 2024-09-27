package com.example.demo.exception;

import org.springframework.security.core.AuthenticationException;

public class MissingTOTPKeyException extends AuthenticationException {
    public MissingTOTPKeyException(String msg) {
        super(msg);
    }
}