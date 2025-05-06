package ru.netology.cloudStorage.security;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import ru.netology.cloudStorage.enums.Role;

import java.util.Collection;
import java.util.Set;

@Slf4j
@Getter
@Setter
public class JwtAuthentication implements Authentication {

    private boolean authenticated;
    private String username;
    private String firstName;
    private Set<Role> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        log.info("Fetching authorities for user: {}", username);
        return roles;
    }

    @Override
    public Object getCredentials() {
        log.info("Fetching credentials for user: {}", username);
        return null;
    }

    @Override
    public Object getDetails() {
        log.info("Fetching details for user: {}", username);
        return null;
    }

    @Override
    public Object getPrincipal() {
        log.info("Fetching principal for user: {}", username);
        return username;
    }

    @Override
    public boolean isAuthenticated() {
        log.info("Checking authentication status for user: {}", username);
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        log.info("Setting authentication status to {} for user: {}", isAuthenticated, username);
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        log.info("Fetching name for user: {}", username);
        return username;
    }
}
