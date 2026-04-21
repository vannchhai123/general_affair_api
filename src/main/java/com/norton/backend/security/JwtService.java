package com.norton.backend.security;

import com.norton.backend.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {
  private final JwtProperties jwtProperties;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> resolver) {
    return resolver.apply(extractAllClaims(token));
  }

  public Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    return extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {
    return extractClaim(token, Claims::getExpiration).before(new Date());
  }

  public boolean isRefreshTokenValid(String token) {
    return !isTokenExpired(token);
  }

  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {

    List<String> authorities =
        userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

    return Jwts.builder()
        .claims(extraClaims)
        .subject(userDetails.getUsername())
        .claim("authorities", authorities)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
        .signWith(getSigningKey())
        .compact();
  }

  public String generateRefreshToken(UserDetails user) {
    return Jwts.builder()
        .subject(user.getUsername())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshExpiration()))
        .signWith(getSigningKey())
        .compact();
  }

  public String generateQrSessionKioskToken(String sessionId, Duration ttl) {
    long now = System.currentTimeMillis();

    return Jwts.builder()
        .subject("qr-session-kiosk")
        .claim("sessionId", sessionId)
        .claim("purpose", "qr-kiosk")
        .issuedAt(new Date(now))
        .expiration(new Date(now + ttl.toMillis()))
        .signWith(getSigningKey())
        .compact();
  }
}
