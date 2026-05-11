package com.gabinete.psicologico_api.controller;

import com.gabinete.psicologico_api.dto.LoginRequest;
import com.gabinete.psicologico_api.dto.LoginResponse;
import com.gabinete.psicologico_api.security.PsicologoPrincipal;
import com.gabinete.psicologico_api.security.SecurityUtils;
import com.gabinete.psicologico_api.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.authenticate(request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        PsicologoPrincipal principal = SecurityUtils.getCurrentPrincipal();
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(principal);
    }
}
