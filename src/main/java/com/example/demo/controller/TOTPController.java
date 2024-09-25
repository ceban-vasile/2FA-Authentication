package com.example.demo.controller;

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

    public TOTPController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/users")
    public @ResponseBody
    Map<String, Object> createUser(@RequestBody User user) throws Throwable {
        User savedUser = userService.createUser(user);
        savedUser.setPassword("");  // Clear password for security

        // Generate OTP protocol for QR Code
        String otpProtocol = userService.generateOTPProtocol(savedUser.getEmail());
        String qrCode = userService.generateQRCode(otpProtocol);

        // Return both the user and the QR code
        Map<String, Object> response = new HashMap<>();
        response.put("user", savedUser);
        response.put("qrCode", qrCode);

        return response;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        Optional<User> userOpt = userService.authenticate(user.getEmail(), user.getPassword());

        if (userOpt.isPresent()) {
            // Generate a token or return user details as needed
            return ResponseEntity.ok("Login Successful"); // Replace with your token generation
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping(value = "/qrcode/validate/{email}")
    public boolean validateTotp(@PathVariable("email") String email, @RequestBody String requestJson) {
        JSONObject json = new JSONObject(requestJson);
        return userService.validateTotp(email, Integer.parseInt(json.getString("totpKey")));
    }

}
