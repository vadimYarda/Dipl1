package ru.netology.cloudStorage.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloudStorage.entity.File;
import ru.netology.cloudStorage.entity.User;
import ru.netology.cloudStorage.exception.FileNotFoundException;
import ru.netology.cloudStorage.exception.InvalidInputDataException;
import ru.netology.cloudStorage.exception.UserNotFoundException;
import ru.netology.cloudStorage.repository.FileRepository;
import ru.netology.cloudStorage.repository.UserRepository;


import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FileService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    public void uploadFile(@NonNull MultipartFile file, String fileName) {
        if (file.isEmpty()) {
            throw new FileNotFoundException("File not found", 0);
        }
        Long userId = getAuthorizedUser().getId();

        if (fileRepository.findFileByUserIdAndFileName(userId, fileName).isPresent()) {
            throw new InvalidInputDataException("This file name already exists. Please choose another file name", userId);
        }

        String hash = getHashOfFile(file);
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new InvalidInputDataException("Can't read the file bytes", userId);
        }

        fileRepository.save(File.builder()
                .hash(hash)
                .fileName(fileName)
                .type(file.getContentType())
                .size(file.getSize())
                .fileByte(fileBytes)
                .createdDate(LocalDateTime.now())
                .user(User.builder().id(userId).build())
                .build());
    }

    public File downloadFile(String fileName) {
        Long userId = getAuthorizedUser().getId();
        return getFileFromStorage(fileName, userId);
    }

    public void editFileName(String fileName, User file) {
        Long userId = getAuthorizedUser().getId();
        File existingFile = getFileFromStorage(fileName, userId);
        existingFile.setFileName(file.getFileName());
        fileRepository.save(existingFile);
    }

    public void deleteFile(String fileName) {
        Long userId = getAuthorizedUser().getId();
        File file = getFileFromStorage(fileName, userId);
        file.setDelete(true);
        file.setUpdatedDate(LocalDateTime.now());
        fileRepository.save(file);
    }

    public List<File> getAllFiles(int limit) {
        Long userId = getAuthorizedUser().getId();
        return fileRepository.findFilesByUserIdWithLimit(userId, limit).stream()
                .filter(file -> !file.isDelete())
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private String getHashOfFile(MultipartFile file) {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream fis = file.getInputStream()) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                md.update(buffer, 0, read);
            }
        }
        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private File getFileFromStorage(String fileName, Long userId) {
        return fileRepository.findFileByUserIdAndFileName(userId, fileName)
                .orElseThrow(() -> new FileNotFoundException("File in storage with file name: " + fileName + " not found! User ID is:", userId));
    }

    public User getAuthorizedUser() {
        final String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findUserByLogin(login).orElseThrow(() ->
                new UserNotFoundException("User not found by login", 0));
    }
}
