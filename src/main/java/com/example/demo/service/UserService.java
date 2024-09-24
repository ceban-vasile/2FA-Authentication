package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import com.example.demo.security.TOTPAuthenticator;
import com.example.demo.repository.UserRepository;
import com.example.demo.model.User;
import com.example.demo.exception.MissingTOTPKeyAuthenticatorException;

@Component
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TOTPAuthenticator totpAuthenticator;

    @Autowired
    private Environment env;

    @Bean
    private PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    public User createUser(User user) {
        user.setPassword(encoder().encode(user.getPassword()));
        user.setSecret(totpAuthenticator.generateSecret());
        return userRepository.save(user);
    }

    public String generateOTPProtocol(String email) {
        User user = userRepository.findByEmail(email);
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", email, email + "@domain.com", user.getSecret(),env.getRequiredProperty("app.application.name"));
    }

    public String generateQRCode(String otpProtocol) throws Throwable {
        return totpAuthenticator.generateQRCode(otpProtocol);
    }

    public boolean validateTotp(String email, Integer totpKey) {
        User user = userRepository.findByEmail(email);
        String secret = user.getSecret();
        if (StringUtils.hasText(secret)) {
            if (totpKey != null) {
                try {
                    if (!totpAuthenticator.verifyCode(secret, totpKey, Integer.parseInt(env.getRequiredProperty("2fa.application.time")))) {
                        System.out.printf("Code %d was not valid", totpKey);
                        throw new BadCredentialsException(
                                "Invalid TOTP code");
                    }
                } catch (InvalidKeyException | NoSuchAlgorithmException e) {
                    throw new InternalAuthenticationServiceException(
                            "TOTP code verification failed", e);
                }
            } else {
                throw new MissingTOTPKeyAuthenticatorException(
                        "TOTP code is mandatory");
            }
        }
        return true;
    }
}