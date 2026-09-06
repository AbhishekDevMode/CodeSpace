package com.email.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key key=Keys.hmacShaKeyFor("YourSecretKeyForJwtSigningMustBeAtLeast32BytesLong".getBytes());

    private final long jwtExpirationInMs=86400000;

    public String generateToken(Authentication authentication){
        String username=authentication.getName();
        Date now= new Date();
        Date expiryDate=new Date(now.getTime()+jwtExpirationInMs);

        return Jwts.builder().setSubject(username).setIssuedAt(now).setExpiration(expiryDate).signWith(key, SignatureAlgorithm.HS256).compact();
    }
}
