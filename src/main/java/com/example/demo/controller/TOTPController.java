package com.example.demo.controller;

import jakarta.validation.Valid;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.User;
import com.example.demo.service.UserService;

import java.util.HashMap;
import java.util.Map;

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



//    @GetMapping(value = "/qrcode/get/{email}")
//    public ResponseEntity<String> generateQRCode(@PathVariable("email") String email) throws Throwable {
//        String otpProtocol = userService.generateOTPProtocol(email);
//        String qrCode = userService.generateQRCode(otpProtocol); // Assume this returns a Base64-encoded QR code
//        return ResponseEntity.ok(qrCode);  // Return the QR code string in response
//    }

    @PostMapping(value = "/qrcode/validate/{email}")
    public boolean validateTotp(@PathVariable("email") String email, @Valid @RequestBody String requestJson) {
        JSONObject json = new JSONObject(requestJson);
        return userService.validateTotp(email, Integer.parseInt(json.getString("totpKey")));
    }

}
