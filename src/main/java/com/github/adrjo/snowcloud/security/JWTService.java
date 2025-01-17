package com.github.adrjo.snowcloud.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
public class JWTService {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    private JWTService(@Value("${snowcloud.jwtsecret}") String secret) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).withIssuer("auth0").build();
    }

    public String generate(UUID userId) {
        return JWT.create()
                .withIssuer("auth0")
                .withSubject(userId.toString())
                .sign(algorithm);
    }

    public UUID verify(String token) {
        DecodedJWT jwt = verifier.verify(token);

        return UUID.fromString(jwt.getSubject());
    }
}
