package ru.netology.cloudStorage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.netology.cloudStorage.DTO.UserDTO;
import ru.netology.cloudStorage.entity.User;
import ru.netology.cloudStorage.model.Token;
import ru.netology.cloudStorage.service.AuthenticationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public Token login(@RequestBody UserDTO userDTO) {
        User user = convertToUser(userDTO);
        return authenticationService.login(user);
    }

    @PostMapping("/logout")
    public String logout(@RequestHeader("Authorization") String authToken,
                         HttpServletRequest request, HttpServletResponse response) {
        return authenticationService.logout(authToken, request, response);
    }

    private User convertToUser(UserDTO userDTO) {
        User user = new User();
        user.setLogin(userDTO.getLogin());
        user.setPassword(userDTO.getPassword());
        return user;
    }
}
