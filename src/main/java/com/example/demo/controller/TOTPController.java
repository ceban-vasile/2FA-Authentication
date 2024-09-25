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
@RequestMapping("/2fa")
public class TOTPController {

    @Autowired
    private final UserService userService;
    @Autowired
    private final JwtUtil jwtUtil;

    public TOTPController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil =jwtUtil;
    }

    @PostMapping(value = "/users")
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        Optional<User> userOpt = userService.authenticate(user.getEmail(), user.getPassword());

        if (userOpt.isPresent()) {
            return ResponseEntity.ok("Login Successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping(value = "/qrcode/validate/{email}")
    public ResponseEntity<?> validateTotp(@PathVariable("email") String email, @RequestBody String requestJson) {
        JSONObject json = new JSONObject(requestJson);
        int totpKey = Integer.parseInt(json.getString("totpKey"));

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

}
