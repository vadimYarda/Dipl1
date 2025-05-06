package ru.netology.cloudStorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import ru.netology.cloudStorage.entity.User;
import ru.netology.cloudStorage.exception.InvalidInputDataException;
import ru.netology.cloudStorage.exception.UserNotFoundException;
import ru.netology.cloudStorage.model.Token;
import ru.netology.cloudStorage.repository.UserRepository;
import ru.netology.cloudStorage.security.JwtProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public Token login(User user) {
        log.info("Login attempt for user: {}", user.getLogin());
        User userFromStorage = findUserInStorage(user.getLogin());
        if (isEquals(user, userFromStorage)) {
            String accessToken = jwtProvider.generateAccessToken(userFromStorage);
            log.info("User {} successfully logged in.", user.getLogin());
            return new Token(accessToken);
        } else {
            log.warn("Login failed for user: {}", user.getLogin());
            throw new InvalidInputDataException("Wrong password", 0);
        }
    }

    public String logout(String authToken, HttpServletRequest request, HttpServletResponse response) {
        log.info("Logout attempt with token: {}", authToken);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = findUserInStorage(auth.getName());
        SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
        if (user != null) {
            securityContextLogoutHandler.logout(request, response, auth);
            jwtProvider.addAuthTokenInBlackList(authToken);
            log.info("User {} successfully logged out.", user.getLogin());
            return user.getLogin();
        }
        log.warn("Logout failed for token: {}", authToken);
        return null;
    }

    private User findUserInStorage(String login) {
        log.info("Fetching user from storage with login: {}", login);
        return userRepository.findUserByLogin(login).orElseThrow(() -> {
            log.error("User not found by login: {}", login);
            return new UserNotFoundException("User not found by login", 0);
        });
    }

    private boolean isEquals(User user, User userFromDatabase) {
        boolean passwordsMatch = passwordEncoder.matches(user.getPassword(), userFromDatabase.getPassword());
        log.info("Password match for user {}: {}", user.getLogin(), passwordsMatch);
        return passwordsMatch;
    }
}
