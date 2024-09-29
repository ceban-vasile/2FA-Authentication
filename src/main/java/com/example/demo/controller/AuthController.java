package com.example.demo.controller;

import com.example.demo.dto.HistoryDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.History;
import com.example.demo.model.User;
import com.example.demo.service.HistoryService;
import com.example.demo.service.JwtManager;
import com.example.demo.service.UserService;
import com.example.demo.service.TOTPService;
import com.google.zxing.WriterException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/2fa")
@RequiredArgsConstructor
public class AuthController {

    private final HistoryService historyService;

    private final UserService userService;
    private final TOTPService totpService;
    private final JwtManager jwtManager;

    @GetMapping("/thef/{id}")
    public ResponseEntity<HistoryDTO> getHistory(@PathVariable Long id) throws Exception {
        return historyService.history(id)
                .map(history -> ResponseEntity.ok(new HistoryDTO(history)))
                .orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/users/authenticate")
    public ResponseEntity<String> login(@RequestBody @Valid User user) throws UserNotFoundException {
        userService.authenticateUser(user.getEmail(), user.getPassword());
        return ResponseEntity.ok("Login Successful");
    }


    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody @Valid User user) throws IOException, WriterException {
        User savedUser = userService.createUser(user);
        String otpProtocol = totpService.generateOTPProtocol(savedUser);
        String qrCode = totpService.generateQRCode(otpProtocol);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "user", new UserDTO(savedUser.getEmail()),
                "qrCode", qrCode
        ));
    }

    @PostMapping("/totp/validate")
    public ResponseEntity<Map<String, Object>> validateTotp(@RequestBody Map<String, String> request) throws UserNotFoundException, NoSuchAlgorithmException, InvalidKeyException {
        String email = request.get("email");
        int totpKey = Integer.parseInt(request.get("totpKey"));

//        User user = userService.getUserByEmail(email);
//        totpService.validateTotp(user, totpKey);

        String accessToken = jwtManager.generateAccessToken(email);
        String refreshToken = jwtManager.generateRefreshToken(email);

        return ResponseEntity.ok(Map.of(
                "message", "2FA Verified",
                "accessToken", accessToken,
                "refreshToken", refreshToken
        ));
    }

    @PostMapping("/tokens/validate")
    public ResponseEntity<String> validateJwt(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        jwtManager.validateAccessToken(token);
        return ResponseEntity.ok("JWT is valid.");
    }

    @PostMapping("/tokens/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        String newAccessToken = jwtManager.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }
}