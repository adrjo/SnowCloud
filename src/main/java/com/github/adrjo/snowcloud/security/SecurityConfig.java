package com.github.adrjo.snowcloud.security;

import com.github.adrjo.snowcloud.auth.AuthRepository;
import com.github.adrjo.snowcloud.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthService authService;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfig(AuthService authService, AuthRepository authRepository, PasswordEncoder encoder) {
        this.authService = authService;
        this.authRepository = authRepository;
        this.passwordEncoder = encoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, OAuthSuccessHandler oAuthSuccessHandler) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/share/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)) // Allow session for OAuth2
                .oauth2Login(oauth -> oauth.successHandler(oAuthSuccessHandler)) // Enable OAuth2 login
                .authenticationProvider(getAuthenticationProvider()) // Custom auth provider for JWT
                .addFilterAfter(new VerifyOAuthFilter(authRepository), OAuth2LoginAuthenticationFilter.class)
                .addFilterAfter(new VerifyJwtFilter(authService), VerifyOAuthFilter.class); // verify jwt only if oauth is not used

        return http.build();
    }

    @Bean
    public AuthenticationProvider getAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(authService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}

