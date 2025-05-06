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
        log.info("Starting file upload for user: {}", getAuthorizedUser().getLogin());
        if (file.isEmpty()) {
            log.error("File not found for upload.");
            throw new FileNotFoundException("File not found", 0);
        }
        Long userId = getAuthorizedUser().getId();

        if (fileRepository.findFileByUserIdAndFileName(userId, fileName).isPresent()) {
            log.warn("File with name '{}' already exists for user: {}", fileName, userId);
            throw new InvalidInputDataException("This file name already exists. Please choose another file name", userId);
        }

        String hash = getHashOfFile(file);
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            log.error("Error reading file bytes: {}", e.getMessage());
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
        log.info("File '{}' uploaded successfully for user: {}", fileName, userId);
    }

    public File downloadFile(String fileName) {
        log.info("Starting file download for user: {}", getAuthorizedUser().getLogin());
        Long userId = getAuthorizedUser().getId();
        File file = getFileFromStorage(fileName, userId);
        log.info("File '{}' downloaded successfully for user: {}", fileName, userId);
        return file;
    }

    public void editFileName(String fileName, User file) {
        log.info("Starting file name edit for user: {}", getAuthorizedUser().getLogin());
        Long userId = getAuthorizedUser().getId();
        File existingFile = getFileFromStorage(fileName, userId);
        existingFile.setFileName(file.getRole());
        fileRepository.save(existingFile);
        log.info("File name edited successfully for user: {}", userId);
    }

    public void deleteFile(String fileName) {
        log.info("Starting file deletion for user: {}", getAuthorizedUser().getLogin());
        Long userId = getAuthorizedUser().getId();
        File file = getFileFromStorage(fileName, userId);
        file.setDelete(true);
        file.setUpdatedDate(LocalDateTime.now());
        fileRepository.save(file);
        log.info("File '{}' deleted successfully for user: {}", fileName, userId);
    }

    public List<File> getAllFiles(int limit) {
        log.info("Fetching all files for user: {}", getAuthorizedUser().getLogin());
        Long userId = getAuthorizedUser().getId();
        List<File> files = fileRepository.findFilesByUserIdWithLimit(userId, limit).stream()
                .filter(file -> !file.isDelete())
                .collect(Collectors.toList());
        log.info("Fetched {} files for user: {}", files.size(), userId);
        return files;
    }

    @SneakyThrows
    private String getHashOfFile(MultipartFile file) {
        log.info("Calculating hash for file: {}", file.getOriginalFilename());
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
        String hash = result.toString();
        log.info("Hash calculated: {}", hash);
        return hash;
    }

    private File getFileFromStorage(String fileName, Long userId) {
        log.info("Retrieving file '{}' from storage for user: {}", fileName, userId);
        return fileRepository.findFileByUserIdAndFileName(userId, fileName)
                .orElseThrow(() -> {
                    log.error("File '{}' not found in storage for user: {}", fileName, userId);
                    return new FileNotFoundException("File in storage with file name: " + fileName + " not found! User ID is:", userId);
                });
    }

    public User getAuthorizedUser() {
        final String login = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Fetching authorized user with login: {}", login);
        return userRepository.findUserByLogin(login).orElseThrow(() -> {
            log.error("User not found by login: {}", login);
            return new UserNotFoundException("User not found by login", 0);
        });
    }
}
