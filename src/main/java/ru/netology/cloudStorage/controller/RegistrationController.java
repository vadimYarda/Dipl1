package ru.netology.cloudStorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.netology.cloudStorage.DTO.UserDTO;
import ru.netology.cloudStorage.entity.User;
import ru.netology.cloudStorage.service.RegistrationService;
import ru.netology.cloudStorage.utils.MapperUtils;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final MapperUtils mapperUtils;

    public RegistrationController(RegistrationService registrationService, RegistrationService registrationService1, MapperUtils mapperUtils) {
        this.registrationService = registrationService1;
        this.mapperUtils = mapperUtils;
    }

    @PostMapping("/register")
    public UserDTO registerUser(@RequestBody UserDTO userDTO) {
        log.info("Registering new user: {}", userDTO.getLogin());
        User user = mapperUtils.toUserEntity(userDTO);
        User registeredUser = registrationService.registerUser(user);
        log.info("User registered successfully: {}", registeredUser.getLogin());
        return mapperUtils.toUserDto(registeredUser);
    }

    @GetMapping("/{id}")
    public UserDTO getUser(@PathVariable Long id) {
        log.info("Fetching user with ID: {}", id);
        User user = registrationService.getUser(id);
        log.info("User fetched successfully: {}", user.getLogin());
        return mapperUtils.toUserDto(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        log.info("Deleting user with ID: {}", id);
        registrationService.deleteUser(id);
        log.info("User with ID {} deleted successfully.", id);
    }
}
