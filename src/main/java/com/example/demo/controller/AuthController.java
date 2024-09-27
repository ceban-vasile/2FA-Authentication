package com.example.demo.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.User;
import com.example.demo.service.JwtManager;
import com.example.demo.service.UserService;
import com.example.demo.service.TOTPService;
import com.google.zxing.WriterException;
import jakarta.validation.Valid;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/2fa")
public class AuthController {

    private final UserService userService;
    private final TOTPService totpService;
    private final JwtManager jwtManager;

    public AuthController(UserService userService, TOTPService totpService, JwtManager jwtManager) {
        this.userService = userService;
        this.totpService = totpService;
        this.jwtManager = jwtManager;
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody @Valid User user) throws IOException, WriterException {
        User savedUser = userService.createUser(user);

        String otpProtocol = totpService.generateOTPProtocol(savedUser);
        String qrCode = totpService.generateQRCode(otpProtocol);

        Map<String, Object> response = new HashMap<>();
        UserDTO userDTO = new UserDTO(savedUser.getEmail());
        response.put("user", userDTO);
        response.put("qrCode", qrCode);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/users/authenticate")
    public ResponseEntity<?> login(@RequestBody @Valid User user) throws UserNotFoundException {
        User authenticatedUser = userService.authenticateUser(user.getEmail(), user.getPassword());
        return ResponseEntity.ok("Login Successful");
    }

    @PostMapping("/totp/validate")
    public ResponseEntity<?> validateTotp(@RequestBody String requestJson) throws UserNotFoundException, NoSuchAlgorithmException, InvalidKeyException {
        JSONObject request = new JSONObject(requestJson);
        String email = request.getString("email");
        int totpKey = Integer.parseInt(request.getString("totpKey"));

        User user = userService.getUserByEmail(email);
        boolean isValidTotp = totpService.validateTotp(user, totpKey);

        if (isValidTotp) {
            String accessToken = jwtManager.generateAccessToken(email);
            String refreshToken = jwtManager.generateRefreshToken(email);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "2FA Verified");
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid 2FA code");
        }
    }

    @PostMapping("/tokens/validate")
    public ResponseEntity<?> validateJwt(@RequestBody String requestJson) {
        JSONObject request = new JSONObject(requestJson);
        String token = request.getString("token");
        jwtManager.validateAccessToken(token);
        return ResponseEntity.ok("JWT is valid.");
    }

    @PostMapping("/tokens/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody String requestJson) {
        JSONObject request = new JSONObject(requestJson);
        String refreshToken = request.getString("refreshToken");

        String newAccessToken = jwtManager.refreshAccessToken(refreshToken);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
        return ResponseEntity.ok(response);
    }
}