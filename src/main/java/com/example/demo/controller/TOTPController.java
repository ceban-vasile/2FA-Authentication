package com.example.demo.controller;

import com.example.demo.service.JwtUtil;
import jakarta.validation.Valid;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.User;
import com.example.demo.service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/2fa")
public class TOTPController {

    @Autowired
    private final UserService userService;
    @Autowired
    private final JwtUtil jwtUtil;

    public TOTPController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/users")
    public @ResponseBody
    Map<String, Object> createUser(@RequestBody User user) throws Throwable {
        User savedUser = userService.createUser(user);
        savedUser.setPassword("");  // Clear password for security

        String otpProtocol = userService.generateOTPProtocol(savedUser.getEmail());
        String qrCode = userService.generateQRCode(otpProtocol);

        Map<String, Object> response = new HashMap<>();
        response.put("user", savedUser);
        response.put("qrCode", qrCode);

        return response;
    }

    @PostMapping("/users/authenticate")
    public ResponseEntity<?> login(@RequestBody User user) {
        Optional<User> userOpt = userService.authenticate(user.getEmail(), user.getPassword());
        return userOpt.isPresent()
                ? ResponseEntity.ok("Login Successful")
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }

    @PostMapping("/totp/validate")
    public ResponseEntity<?> validateTotp(@RequestBody JSONObject requestJson) {
        String email = requestJson.getString("email");
        int totpKey = Integer.parseInt(requestJson.getString("totpKey"));

        boolean isValidTotp = userService.validateTotp(email, totpKey);

        if (isValidTotp) {
            String token = jwtUtil.generateToken(email);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "2FA Verified");
            response.put("token", token);

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid 2FA code");
        }
    }

    @PostMapping("/tokens/validate")
    public ResponseEntity<?> validateJwt(@RequestBody JSONObject requestJson) {
        String token = requestJson.getString("token");
        jwtUtil.validateToken(token);
        return ResponseEntity.ok("JWT is valid.");
    }
}
