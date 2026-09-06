package com.email.backend.controller;


import com.email.backend.dto.AuthResponse;
import com.email.backend.dto.LoginRequest;
import com.email.backend.dto.SignupRequest;
import com.email.backend.repository.UserRepository;
import com.email.backend.service.AuthService;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {


    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private AuthService authService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody SignupRequest request) {
        String response = authService.registerUser(request);
        return ResponseEntity.ok(response);
    }


}
