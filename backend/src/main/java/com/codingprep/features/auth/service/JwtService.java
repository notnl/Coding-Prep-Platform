package com.codingprep.features.auth.service;

import com.codingprep.features.auth.dto.LoginRequest;
import com.codingprep.features.auth.models.AuthenticationUser;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import io.jsonwebtoken.*;

import io.jsonwebtoken.security.Keys;


@Service
public class JwtService {

    @Value("${jwt.secret.key}")
    private String secret;

    //private static final long ACCESS_TOKEN_EXPIRATION = 120 * 60 * 1000; // 120 min 
    private static final long ACCESS_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 days // 120 min 
                                                                         
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 days

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }


    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid(String token, AuthenticationUser userDetails) {
        final String username = extractUsername(token);

        boolean resUsername = username.equals(userDetails.getUsername());
        boolean tokenExpired = !isTokenExpired(token);

        return resUsername && tokenExpired;
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateAccessToken(AuthenticationUser userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        extraClaims.put("roles", roles);
        extraClaims.put("userId", userDetails.getId());

        
        //if (userDetails instanceof AuthenticationUser) {
        //    AuthenticationUser authUser = (AuthenticationUser) userDetails;
        //    extraClaims.put("isTwoFactorEnabled", authUser.getTwoFactorEnabled());
        //}

        return buildToken(extraClaims, userDetails, ACCESS_TOKEN_EXPIRATION);
    }

    public String generateRefreshToken(AuthenticationUser userDetails) {
        return buildToken(new HashMap<>(), userDetails, REFRESH_TOKEN_EXPIRATION);
    }


    private String buildToken(
            Map<String, Object> extraClaims,
            AuthenticationUser userDetails,
            long expiration
    ) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }


}
