package com.plenkov.cloudstorage.exception;

public class UserAlreadyExistException extends RuntimeException {
    public UserAlreadyExistException(String name) {
        super("User with username '" + name + "' already exists");
    }
}
