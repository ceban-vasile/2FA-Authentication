package com.example.demo.service;

import com.example.demo.exception.MissingTOTPKeyException;
import com.example.demo.model.User;
import com.google.zxing.WriterException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class TOTPService {

    private final TOTPManager totpManager;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${totp.time-step-seconds}")
    private int totpTimeStepSeconds;

    public String generateSecret() {
        return totpManager.generateSecret();
    }

    public String generateOTPProtocol(User user) {
        return UriComponentsBuilder
                .fromUriString("otpauth://totp/{application}:{userEmail}")
                .queryParam("secret", user.getSecret())
                .queryParam("issuer", applicationName)
                .buildAndExpand(applicationName, user.getEmail())
                .toUriString();
    }

    public String generateQRCode(String otpProtocol) throws IOException, WriterException {
        return totpManager.generateQRCode(otpProtocol);
    }

    public void validateTotp(User user, Integer totpKey) throws NoSuchAlgorithmException, InvalidKeyException {
        if (totpKey == null) {
            throw new MissingTOTPKeyException("TOTP code is mandatory");
        }

        if (!totpManager.verifyCode(user.getSecret(), totpKey, totpTimeStepSeconds)) {
            throw new BadCredentialsException("Invalid TOTP code");
        }
    }
}