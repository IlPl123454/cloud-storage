package com.plenkov.cloudstorage.exception;

import lombok.Getter;

@Getter
public class FileNotFoundException extends RuntimeException {
    private final String path;

    public FileNotFoundException(String message, String path) {
        super(message);
        this.path = path;
    }
}
