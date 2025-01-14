package com.github.adrjo.snowcloud.auth;

import com.github.adrjo.snowcloud.util.CryptoUtils;
import lombok.Data;

@Data
public class User {
    private final String email;
    private final String username;
    private final String hashedPassword;

    public User(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.hashedPassword = CryptoUtils.hashPassword(password);
    }
}
