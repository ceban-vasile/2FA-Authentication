package com.example.demo.service;

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
import java.util.Optional;

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
    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setSecret(totpAuthenticator.generateSecret());
        return userRepository.save(user);
    }

    public Optional<User> authenticate(String email, String password) {
        Optional<User> userOpt = Optional.ofNullable(userRepository.findByEmail(email));
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Compare the encrypted password with the raw password
            if (passwordEncoder.matches(password, user.getPassword())) {
                return userOpt;
            }
        }
        return Optional.empty();
    }

    public String generateOTPProtocol(String email) {
        User user = userRepository.findByEmail(email);

        // Generate the otpauth URI
        String issuer = env.getRequiredProperty("spring.application.name");
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                issuer,      // Issuer name in the label
                email,       // Email for easy identification
                user.getSecret(),  // The user's TOTP secret
                issuer);     // Issuer name in the query param
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
                    if (!totpAuthenticator.verifyCode(secret, totpKey, Integer.parseInt(env.getRequiredProperty("spring.application.time")))) {
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