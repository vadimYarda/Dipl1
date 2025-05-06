package ru.netology.cloudStorage.security;

import io.jsonwebtoken.Claims;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.netology.cloudStorage.enums.Role;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JwtUtils {

    public static JwtAuthentication generate(Claims claims) {
        log.info("Generating JwtAuthentication from claims.");
        JwtAuthentication jwtInfoToken = new JwtAuthentication();
        jwtInfoToken.setRoles(getRoles(claims));
        jwtInfoToken.setUsername(claims.getSubject());
        log.info("JwtAuthentication generated for user: {}", claims.getSubject());
        return jwtInfoToken;
    }

    private static Set<Role> getRoles(Claims claims) {
        log.info("Extracting roles from claims.");
        List<String> roles = claims.get("roles", List.class);
        Set<Role> roleSet = roles.stream()
                .map(Role::valueOf)
                .collect(Collectors.toSet());
        log.info("Roles extracted: {}", roleSet);
        return roleSet;
    }
}
