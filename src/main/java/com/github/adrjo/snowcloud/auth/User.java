package com.github.adrjo.snowcloud.auth;

import com.github.adrjo.snowcloud.cloud.CloudFile;
import com.github.adrjo.snowcloud.util.CryptoUtils;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity(name = "cloud_user")
@Data
public class User {

    @Id
    private final UUID id;

    private final String email;
    private final String username;
    private final String hashedPassword;

    @OneToMany(mappedBy = "user")
    private List<CloudFile> files;

    public User(String email, String username, String password) {
        this.id = UUID.randomUUID();
        this.email = email;
        this.username = username;
        this.hashedPassword = CryptoUtils.hashPassword(password);
    }
}
