package com.example.demo.service;

import com.example.demo.exception.MissingTOTPKeyException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.TOTPAuthenticator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TOTPAuthenticator totpAuthenticator;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${totp.time-step-seconds}")
    private int totpTimeStepSeconds;

    @Transactional
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setSecret(totpAuthenticator.generateSecret());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public void authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }
    }

    @Transactional(readOnly = true)
    public String generateOTPProtocol(String email) throws UserNotFoundException {
        User user = getUserByEmail(email);
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                applicationName,
                email,
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

    @Transactional(readOnly = true)
    public boolean validateTotp(String email, Integer totpKey) throws UserNotFoundException {
        User user = getUserByEmail(email);
        String secret = user.getSecret();

        if (totpKey == null) {
            throw new MissingTOTPKeyException("TOTP code is mandatory");
        }

        try {
            if (!totpAuthenticator.verifyCode(secret, totpKey, totpTimeStepSeconds)) {
                throw new BadCredentialsException("Invalid TOTP code");
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("TOTP code verification failed", e);
        }
        return true;
    }

    private User getUserByEmail(String email) throws UserNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }
}