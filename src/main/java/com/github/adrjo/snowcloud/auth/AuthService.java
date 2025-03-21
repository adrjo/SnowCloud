package com.github.adrjo.snowcloud.auth;

import com.github.adrjo.snowcloud.cloud.CloudService;
import com.github.adrjo.snowcloud.security.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService implements UserDetailsService {
    private final AuthRepository repository;
    private final CloudService cloudService;

    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;

    private static final int MAX_USERNAME_LENGTH = 16;
    private static final int MIN_USERNAME_LENGTH = 3;

    private static final int MIN_PASS_LENGTH = 8;

    @Autowired
    public AuthService(AuthRepository repository, CloudService cloudService, PasswordEncoder passwordEncoder, JWTService jwtService) {
        this.repository = repository;
        this.cloudService = cloudService;

        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new user
     *
     * @param email user email
     * @param name user name
     * @param pass user password
     * @return the created user data
     */
    public User register(String email, String name, String pass) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail may not be empty.");
        }

        if (name == null || name.length() < MIN_USERNAME_LENGTH || name.length() > MAX_USERNAME_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("Username length must be between %s and %s characters long.",
                            MIN_USERNAME_LENGTH,
                            MAX_USERNAME_LENGTH));
        }

        if (pass == null || pass.length() < MIN_PASS_LENGTH) {
            throw new IllegalArgumentException("Password too short! Min-length: " + MIN_PASS_LENGTH);
        }

        String hashedPassword = passwordEncoder.encode(pass);
        final User user = new User(email, name, hashedPassword);

        repository.save(user);
        cloudService.createRootFolder(user);
        return user;
    }

    /**
     * Logs into a user
     *
     * @param name
     * @param password
     * @return the generated jwt-auth token
     * @throws AuthenticationException on invalid credentials
     */
    public String login(String name, String password) throws AuthenticationException {
        User user = (User) loadUserByUsername(name);

        if (!passwordEncoder.matches(password, user.getHashedPassword())) {
            throw new AuthenticationException("Invalid username or password.");
        }

        return jwtService.generate(user.getId());
    }

    private Optional<User> loadUserByNameOrEmail(String nameOrEmail) {
        return repository.findByUsernameOrEmail(nameOrEmail);
    }

    @Override
    public UserDetails loadUserByUsername(String nameOrEmail) throws UsernameNotFoundException {
        return loadUserByNameOrEmail(nameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password."));
    }

    public Optional<User> verify(String token) {
        try {
            UUID id = jwtService.verify(token);
            return repository.findById(id);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Creates a SnowCloud user via oidc
     * Currently only supports GitHub
     *
     * @param username the username fetched from GitHub
     * @param oidcId the oidcId for the GitHub user
     */
    public void createOAuthUser(String username, String oidcId) {
        Optional<User> existingUser = repository.findByOidcId(oidcId);

        if (existingUser.isPresent()) {
            return;
        }

        final User openIdcUser = User.createOidcUser(username, oidcId, "github.com"); // TODO: find a better home for provider
        repository.save(openIdcUser);
        cloudService.createRootFolder(openIdcUser);
    }
}
