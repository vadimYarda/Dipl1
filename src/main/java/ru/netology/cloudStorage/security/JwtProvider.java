package ru.netology.cloudStorage.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.netology.cloudStorage.entity.User;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class JwtProvider {

    private final SecretKey jwtAccessSecret;

    @Value("${jwt.token-life-time}")
    private int timeLifeToken;
    private final Set<String> blackListToken = new HashSet<>();

    public JwtProvider(
            @Value("${jwt.secret}")
            String jwtAccessSecret) {
        this.jwtAccessSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtAccessSecret));
    }

    public String generateAccessToken(@NonNull User user) {
        log.info("Generating access token for user: {}", user.getLogin());
        LocalDateTime now = LocalDateTime.now();
        Instant accessExpirationInstant = now.plusMinutes(timeLifeToken)
                .atZone(ZoneId.systemDefault()).toInstant();
        Date accessExpiration = Date.from(accessExpirationInstant);
        String token = Jwts.builder()
                .setId(String.valueOf(user.getId()))
                .setSubject(user.getLogin())
                .setExpiration(accessExpiration)
                .signWith(jwtAccessSecret)
                .claim("roles", user.getRoles())
                .compact();
        log.info("Access token generated for user: {}", user.getLogin());
        return token;
    }

    public boolean validateAccessToken(@NonNull String accessToken) {
        log.info("Validating access token.");
        if (blackListToken.contains(accessToken)) {
            log.warn("Token is in blacklist: {}", accessToken);
            return false;
        }

        try {
            Jwts.parserBuilder()
                    .setSigningKey(jwtAccessSecret)
                    .build()
                    .parseClaimsJws(accessToken);
            log.info("Token is valid: {}", accessToken);
            return true;
        } catch (ExpiredJwtException expEx) {
            log.error("Token expired: {}", accessToken, expEx);
        } catch (UnsupportedJwtException unsEx) {
            log.error("Unsupported jwt: {}", accessToken, unsEx);
        } catch (MalformedJwtException mjEx) {
            log.error("Malformed jwt: {}", accessToken, mjEx);
        } catch (SignatureException sEx) {
            log.error("Invalid signature: {}", accessToken, sEx);
        } catch (Exception e) {
            log.error("Invalid token: {}", accessToken, e);
        }
        return false;
    }

    public Claims getAccessClaims(@NonNull String token) {
        log.info("Retrieving claims from token: {}", token);
        return getClaims(token, jwtAccessSecret);
    }

    private Claims getClaims(@NonNull String token, @NonNull Key secret) {
        return Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public void addAuthTokenInBlackList(String authToken) {
        log.info("Adding token to blacklist: {}", authToken);
        blackListToken.add(authToken);
        log.info("Token added to blacklist: {}", authToken);
    }
}
