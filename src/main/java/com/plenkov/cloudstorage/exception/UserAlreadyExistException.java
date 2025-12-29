package com.plenkov.cloudstorage.exception;

import com.plenkov.cloudstorage.config.LogMessage;
import lombok.Getter;

@Getter
public class UserAlreadyExistException extends RuntimeException {
    private final String username;

    public UserAlreadyExistException(String username) {
        super(String.format(LogMessage.EXCEPTION_USER_ALREADY_EXIST, username));
        this.username = username;
    }
}
