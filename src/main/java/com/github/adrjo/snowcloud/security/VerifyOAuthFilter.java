package com.github.adrjo.snowcloud.security;

import com.github.adrjo.snowcloud.auth.AuthRepository;
import com.github.adrjo.snowcloud.auth.AuthService;
import com.github.adrjo.snowcloud.auth.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class VerifyOAuthFilter extends OncePerRequestFilter {

    private final AuthRepository authRepository;

    public VerifyOAuthFilter(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (auth instanceof OAuth2AuthenticationToken token) {
            OAuth2User oAuthUser = token.getPrincipal();

            // get normal user
            User user = authRepository.findByOidcId(oAuthUser.getName()).orElseThrow();

            //set authentication to the user
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                            user,
                            user.getPassword(),
                            user.getAuthorities()
                    ));
        }

        filterChain.doFilter(request, response);
    }
}
