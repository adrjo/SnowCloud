package com.github.adrjo.snowcloud.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.adrjo.snowcloud.cloud.CloudFolder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity(name = "cloud_user")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    private UUID id;

    private String oidcId;

    @Column(unique = true)
    private String email;
    @Column(unique = true)
    private String username;
    private String hashedPassword;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<CloudFolder> files;

    public User(String email, String username, String hashedPassword) {
        this.id = UUID.randomUUID();
        this.email = email;
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.files = new ArrayList<>();
    }

    public static User createOidcUser(String username, String oidcId, String provider) {
        return new User(UUID.randomUUID(),
                oidcId,
                null,
                username + "@" + provider,
                null,
                new ArrayList<>()
                );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return this.hashedPassword;
    }
}
