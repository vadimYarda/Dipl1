package ru.netology.cloudStorage.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorMessage {
    private String message;
    private long id;
}
