package com.gestion.service.JWTService;

import com.gestion.utils.TokenPayload;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;


@Service
@AllArgsConstructor
public class JwtService {
    @Value("${jwt.secret}")
    private final String secretWord;

    // Expiration time in milliseconds
    @Value("${jwt.expiration}")
    private final Long expirationTime;

    public Key getSecretKey() {
        return Keys.hmacShaKeyFor(secretWord.getBytes());
    }

    public String generateToken(TokenPayload payload) {
        return Jwts.builder()
                .subject(payload.email())
                .claim("userId", payload.userId())
                .claim("role", payload.role())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSecretKey())
                .compact();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().verifyWith((SecretKey) getSecretKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
