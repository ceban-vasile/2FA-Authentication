package com.example.demo.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.User;
import com.example.demo.service.JwtUtil;
import com.example.demo.service.UserService;
import com.example.demo.service.TOTPService;
import jakarta.validation.Valid;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/2fa")
public class TOTPController {

    private final UserService userService;
    private final TOTPService totpService;
    private final JwtUtil jwtUtil;

    public TOTPController(UserService userService, TOTPService totpService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.totpService = totpService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody @Valid User user) {
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
    public ResponseEntity<?> validateTotp(@RequestBody String requestJson) throws UserNotFoundException {
        JSONObject request = new JSONObject(requestJson);
        String email = request.getString("email");
        int totpKey = Integer.parseInt(request.getString("totpKey"));

        User user = userService.getUserByEmail(email);
        boolean isValidTotp = totpService.validateTotp(user, totpKey);

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
    public ResponseEntity<?> validateJwt(@RequestBody String requestJson) {
        try {
            JSONObject request = new JSONObject(requestJson);
            String token = request.getString("token");
            jwtUtil.validateToken(token);
            return ResponseEntity.ok("JWT is valid.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid JWT");
        }
    }
}