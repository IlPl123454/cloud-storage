package com.plenkov.cloudstorage.exception;

import lombok.Getter;

@Getter
public class FileAlreadyExistsException extends RuntimeException {
    private final String path;

    public FileAlreadyExistsException(String message, String path) {
        super(message);
        this.path = path;
    }
}