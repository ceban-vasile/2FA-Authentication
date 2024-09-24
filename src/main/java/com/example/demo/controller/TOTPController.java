package com.example.demo.controller;

import jakarta.validation.Valid;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.User;
import com.example.demo.service.UserService;

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
    User createUser(@RequestBody User user) {
        User savedUser = userService.createUser(user);
        savedUser.setPassword("");
        return savedUser;
    }

    @GetMapping(value = "/qrcode/get/{email}")
    public String generateQRCode(@PathVariable("email") String email) throws Throwable {
        String otpProtocol = userService.generateOTPProtocol(email);
        System.out.println(otpProtocol);
        return userService.generateQRCode(otpProtocol);
    }

    @PostMapping(value = "/qrcode/validate/{email}")
    public boolean validateTotp(@PathVariable("email") String email, @Valid @RequestBody String requestJson) {
        JSONObject json = new JSONObject(requestJson);
        return userService.validateTotp(email, Integer.parseInt(json.getString("totpKey")));
    }

}
