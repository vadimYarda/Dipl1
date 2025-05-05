package ru.netology.cloudStorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.netology.cloudStorage.DTO.UserDTO;
import ru.netology.cloudStorage.entity.User;
import ru.netology.cloudStorage.service.RegistrationService;

import jakarta.validation.Valid;
import ru.netology.cloudStorage.utils.MapperUtils;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final MapperUtils mapperUtils;

    @PostMapping("/register")
    public UserDTO registerUser(@RequestBody UserDTO userDTO) {
        User user = mapperUtils.toUserEntity(userDTO);
        User registeredUser = registrationService.registerUser(user);
        return mapperUtils.toUserDto(registeredUser);
    }

    @GetMapping("/{id}")
    public UserDTO getUser(@PathVariable Long id) {
        User user = registrationService.getUser(id);
        return mapperUtils.toUserDto(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        registrationService.deleteUser(id);
    }
}
