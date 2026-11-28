package com.gestion.service.jwt;

import com.gestion.properties.JwtProperties;
import com.gestion.utils.TokenPayload;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Service
public class JwtService {
    public static final String VERSION_CLAIM = "ver";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(TokenPayload payload) {
        return Jwts.builder()
                .subject(payload.email())
                .claim("name", payload.name())
                .claim("userId", payload.userId())
                .claim("commerceId", payload.commerceId())
                .claim("role", payload.role().toString())
                .claim("active", payload.active())
                .claim("sellPointId", payload.sellPointId())
                // version del usuario al emitir el token: JwtFilter la compara con la de la base
                .claim(VERSION_CLAIM, payload.version())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.expiration()))
                .signWith(secretKey)
                .compact();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationMillis() {
        return jwtProperties.expiration();
    }
}
