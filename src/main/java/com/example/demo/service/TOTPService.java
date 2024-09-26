package com.example.demo.service;

import com.example.demo.exception.MissingTOTPKeyException;
import com.example.demo.model.User;
import com.example.demo.security.TOTPAuthenticator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class TOTPService {

    private final TOTPAuthenticator totpAuthenticator;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${totp.time-step-seconds}")
    private int totpTimeStepSeconds;

    public String generateSecret() {
        return totpAuthenticator.generateSecret();
    }

    public String generateOTPProtocol(User user) {
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                applicationName,
                user.getEmail(),
                user.getSecret(),
                applicationName);
    }

    public String generateQRCode(String otpProtocol) {
        try {
            return totpAuthenticator.generateQRCode(otpProtocol);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    public boolean validateTotp(User user, Integer totpKey) {
        if (totpKey == null) {
            throw new MissingTOTPKeyException("TOTP code is mandatory");
        }

        try {
            if (!totpAuthenticator.verifyCode(user.getSecret(), totpKey, totpTimeStepSeconds)) {
                throw new BadCredentialsException("Invalid TOTP code");
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("TOTP code verification failed", e);
        }
        return true;
    }
}