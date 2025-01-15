package com.github.adrjo.snowcloud.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private AuthRepository repository;

    private static final int MAX_LENGTH = 16;
    private static final int MIN_LENGTH = 3;

    private static final int MIN_PASS_LENGTH = 8;

    @Autowired
    public AuthService(AuthRepository repository) {
        this.repository = repository;
    }

    public User register(String email, String name, String pass) {
        if (email.isBlank()) {
            throw new IllegalArgumentException("E-mail may not be empty.");
        }

        if (name.length() < MIN_LENGTH || name.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("Username length must be between %s and %s characters long.", MIN_LENGTH, MAX_LENGTH));
        }

        if (pass.length() < MIN_PASS_LENGTH) {
            throw new IllegalArgumentException("Password too short! Min-length: " + MIN_PASS_LENGTH);
        }

        final User user = new User(email, name, pass);

        repository.save(user);
        return user;
    }
}
