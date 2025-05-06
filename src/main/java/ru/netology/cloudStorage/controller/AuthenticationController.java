package ru.netology.cloudStorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.netology.cloudStorage.DTO.UserDTO;
import ru.netology.cloudStorage.entity.User;
import ru.netology.cloudStorage.model.Token;
import ru.netology.cloudStorage.service.AuthenticationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public Token login(@RequestBody UserDTO userDTO) {
        log.info("Login attempt for user: {}", userDTO.getLogin());
        User user = convertToUser(userDTO);
        Token token = authenticationService.login(user);
        log.info("User {} successfully logged in.", userDTO.getLogin());
        return token;
    }

    @PostMapping("/logout")
    public String logout(@RequestHeader("Authorization") String authToken,
                         HttpServletRequest request, HttpServletResponse response) {
        log.info("Logout attempt with token: {}", authToken);
        String result = authenticationService.logout(authToken, request, response);
        log.info("User successfully logged out with token: {}", authToken);
        return result;
    }

    private User convertToUser(UserDTO userDTO) {
        User user = new User();
        user.setLogin(userDTO.getLogin());
        user.setPassword(userDTO.getPassword());
        return user;
    }
}
