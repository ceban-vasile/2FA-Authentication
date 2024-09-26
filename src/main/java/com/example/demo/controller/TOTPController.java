package com.example.demo.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.exception.UserNotFoundException;
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

        String otpProtocol = userService.generateOTPProtocol(savedUser.getEmail());
        String qrCode = userService.generateQRCode(otpProtocol);

        Map<String, Object> response = new HashMap<>();
        response.put("user", savedUser.getEmail());
        response.put("qrCode", qrCode);

        return response;
    }

    @PostMapping("/users/authenticate")
    public ResponseEntity<?> login(@RequestBody User user) {
        userService.authenticate(user.getEmail(), user.getPassword());
        return ResponseEntity.ok("Login Successful");
    }

    @PostMapping("/totp/validate")
    public ResponseEntity<?> validateTotp(@RequestBody String requestJson) throws UserNotFoundException {
        JSONObject request = new JSONObject(requestJson);
        String email = request.getString("email");
        int totpKey = Integer.parseInt(request.getString("totpKey"));

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
    public ResponseEntity<?> validateJwt(@RequestBody String requestJson) {
        JSONObject request = new JSONObject(requestJson);
        String token = request.getString("token");
        System.out.println(token);
        jwtUtil.validateToken(token);
        return ResponseEntity.ok("JWT is valid.");
    }
}
