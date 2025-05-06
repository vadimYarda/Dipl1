package ru.netology.cloudStorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.netology.cloudStorage.entity.User;
import ru.netology.cloudStorage.enums.Role;
import ru.netology.cloudStorage.exception.UserAlreadyExistsException;
import ru.netology.cloudStorage.exception.UserNotFoundException;
import ru.netology.cloudStorage.repository.UserRepository;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Проверяем, есть ли уже такой пользователь в базе данных. Если есть, бросаем исключение, если нет, регистрируем:
    public User registerUser(User user) {
        log.info("Attempting to register user: {}", user.getLogin());
        userRepository.findUserByLogin(user.getLogin()).ifPresent(s -> {
            log.warn("User already exists: {}", user.getLogin());
            throw new UserAlreadyExistsException("User already exists", user.getId());
        });
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Collections.singleton(Role.ROLE_USER));
        user.setRole(Role.ROLE_USER.getAuthority());
        User registeredUser = userRepository.save(user);
        log.info("User registered successfully: {}", user.getLogin());
        return registeredUser;
    }

    // Ищем пользователя по ID, если нет, бросаем исключение:
    public User getUser(Long id) {
        log.info("Fetching user with ID: {}", id);
        User user = userRepository.findById(id).orElseThrow(() -> {
            log.error("User not found with ID: {}", id);
            return new UserNotFoundException("User not found", id);
        });
        log.info("User fetched successfully: {}", user.getLogin());
        return user;
    }

    // Удаляем пользователя по ID. Если нет, бросаем исключение:
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        User foundUser = userRepository.findById(id).orElseThrow(() -> {
            log.error("User not found with ID: {}", id);
            return new UserNotFoundException("User not found", id);
        });
        userRepository.deleteById(id);
        log.info("User with ID {} deleted successfully.", id);
    }
}
