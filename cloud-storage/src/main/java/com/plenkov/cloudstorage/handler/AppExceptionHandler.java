package com.plenkov.cloudstorage.handler;

import com.plenkov.cloudstorage.dto.ErrorDto;
import com.plenkov.cloudstorage.exception.UserAlreadyExistException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class AppExceptionHandler {
    @ExceptionHandler(UserAlreadyExistException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public ErrorDto handleUserAlreadyExistException(UserAlreadyExistException e) {
        log.error(e.getMessage());
        return new ErrorDto(e.getMessage());
    }
}
