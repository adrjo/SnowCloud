package com.github.adrjo.snowcloud.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService service;

    @Autowired
    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterUserDto userDto) {
        try {
            User user = service.register(userDto.getEmail(), userDto.getName(), userDto.getPassword());
            return ResponseEntity.ok(user.getId());
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginUserDto dto) {
        try {
            String token = service.login(dto.getName(), dto.getPassword());

            return ResponseEntity.ok(token);
        } catch (AuthenticationException | UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @AllArgsConstructor
    @Getter
    public static class LoginUserDto {
        private String name;
        private String password;
    }

    @AllArgsConstructor
    @Getter
    public static class RegisterUserDto {
        private String email;
        private String name;
        private String password;
    }
}
