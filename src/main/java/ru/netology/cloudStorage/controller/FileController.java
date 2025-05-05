package ru.netology.cloudStorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloudStorage.DTO.FileDTO;
import ru.netology.cloudStorage.entity.File;
import ru.netology.cloudStorage.entity.User;
import ru.netology.cloudStorage.service.FileService;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import ru.netology.cloudStorage.utils.MapperUtils;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/files")
public class FileController {
    private final FileService fileService;
    private final MapperUtils mapperUtils;

    @PostMapping("/upload")
    public void uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("fileName") String fileName) {
        fileService.uploadFile(file, fileName);
    }

    @GetMapping("/download/{fileName}")
    public FileDTO downloadFile(@PathVariable String fileName) {
        File file = fileService.downloadFile(fileName);
        return mapperUtils.toFileDto(file);
    }

    @PutMapping("/edit/{fileName}")
    public void editFileName(@PathVariable String fileName, @RequestBody FileDTO fileDTO) {
        User file = mapperUtils.toUserEntity(fileDTO);
        fileService.editFileName(fileName, file);
    }

    @DeleteMapping("/delete/{fileName}")
    public void deleteFile(@PathVariable String fileName) {
        fileService.deleteFile(fileName);
    }

    @GetMapping("/list")
    public List<FileDTO> getAllFiles(@RequestParam(defaultValue = "10") int limit) {
        List<File> files = fileService.getAllFiles(limit);
        return files.stream()
                .map(mapperUtils::toFileDto)
                .collect(Collectors.toList());
    }
}