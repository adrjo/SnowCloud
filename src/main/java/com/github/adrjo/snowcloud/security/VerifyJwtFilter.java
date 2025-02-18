package com.github.adrjo.snowcloud.security;

import com.github.adrjo.snowcloud.auth.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class VerifyJwtFilter extends OncePerRequestFilter {

    private final AuthService authService;

    public VerifyJwtFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (!validHeader(authHeader)) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7);

        authService.verify(token).ifPresent(user -> {
            var auth = new UsernamePasswordAuthenticationToken(user, user.getHashedPassword(), user.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(auth);
        });

        filterChain.doFilter(request, response);
    }

    private boolean validHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authHeader.substring(7);

        return !token.isBlank();
    }
}
