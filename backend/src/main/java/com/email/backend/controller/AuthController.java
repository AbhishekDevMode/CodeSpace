package com.email.backend.controller;

import com.email.backend.dto.AuthResponse;
import com.email.backend.dto.LoginRequest;
import com.email.backend.dto.SignupRequest;
import com.email.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/register", "/signup"})
    public ResponseEntity<String> register(@RequestBody SignupRequest request) {
        String response = authService.registerUser(request);
        return ResponseEntity.ok(response);
    }
}
