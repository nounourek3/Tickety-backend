package com.example.tickety.controllers;

import com.example.tickety.DTOS.AuthRequest;
import com.example.tickety.DTOS.AuthResponse;
import com.example.tickety.config.PasswordEncoderConfig;

import com.example.tickety.entities.User;
import com.example.tickety.repositories.UserRepository;
import com.example.tickety.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")

public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest authRequest) {
        if (userRepository.findByEmail(authRequest.getEmail()).isPresent()){
            return ResponseEntity.badRequest().build();
        }
        User user = new User();
        user.setEmail(authRequest.getEmail());
        user.setPassword(passwordEncoder.encode(authRequest.getPassword()));
        user.setUsername(authRequest.getUsername());
        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getEmail()));
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        Optional<User> userOpt = userRepository.findByEmail(authRequest.getEmail());
        if(userOpt.isEmpty()) return ResponseEntity.status(404).contentType(MediaType.TEXT_PLAIN).body("USER_NOT_FOUND");

        User user = userOpt.get();
        if(!passwordEncoder.matches(authRequest.getPassword(), user.getPassword())){
            return ResponseEntity.status(401).contentType(MediaType.TEXT_PLAIN).body("INVALID_PASSWORD");
        }
        String token = jwtService.generateToken(user.getEmail());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AuthResponse(token, user.getId(), user.getEmail()));
    }
}
