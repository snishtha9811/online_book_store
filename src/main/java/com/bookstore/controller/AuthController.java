package com.bookstore.controller;

import com.bookstore.dto.AuthDTO;
import com.bookstore.dto.AuthResponseDTO;
import com.bookstore.dto.LoginDTO;
import com.bookstore.entity.User;
import com.bookstore.security.JwtTokenProvider;
import com.bookstore.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody AuthDTO authDTO) {
        log.info("Registering new user with username: {}", authDTO.getUsername());

        User user = userService.registerUser(authDTO);

        String token = jwtTokenProvider.generateTokenFromUsername(user.getUsername());

        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(token);
        response.setMessage("User registered successfully");
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Login user
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        log.info("User login attempt with username: {}", loginDTO.getUsername());

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(),
                loginDTO.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);
        User user = userService.getUserByUsername(loginDTO.getUsername());

        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(token);
        response.setMessage("User logged in successfully");
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());

        return ResponseEntity.ok(response);
    }
}

