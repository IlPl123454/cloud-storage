package com.plenkov.cloudstorage.exception;

import lombok.Getter;

@Getter
public class MinioStorageException extends RuntimeException {
    private final String path;

    public MinioStorageException(String message, Throwable cause, String path) {
        super(message, cause);
        this.path = path;
    }
}
