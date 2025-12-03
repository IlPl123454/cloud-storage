package com.plenkov.cloudstorage.handler;

import com.plenkov.cloudstorage.dto.ErrorDto;
import com.plenkov.cloudstorage.exception.AuthException;
import com.plenkov.cloudstorage.exception.FileAlreadyExistsException;
import com.plenkov.cloudstorage.exception.UserAlreadyExistException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class AppExceptionHandler {

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorDto handleMethodArgumentNotValidException(MethodArgumentNotValidException e, BindingResult bindingResult) {
        String message = bindingResult.getAllErrors().getFirst().getDefaultMessage();
        log.error(message);
        return new ErrorDto(message);
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public ErrorDto handleUserAlreadyExistException(UserAlreadyExistException e) {
        log.error(e.getMessage());
        return new ErrorDto("Логин занят");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public ErrorDto handleIllegalArgument(IllegalArgumentException e) {
        log.error(e.getMessage());
        return new ErrorDto("Невалидное тело запроса");
    }

    @ExceptionHandler(AuthException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ErrorDto handleAuthException(AuthException e) {
        log.error(e.getMessage());
        return new ErrorDto("Неверный логин или пароль");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ErrorDto handleException(NoResourceFoundException e) {
        log.error(e.getMessage());
        return new ErrorDto("404. Страница не найдена");
    }


    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(value = HttpStatus.PAYLOAD_TOO_LARGE)
    public ErrorDto handleEMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error(e.getMessage());
        return new ErrorDto("Вы пытаетесь загрузить слишком большой файл. " +
                "Максимальный размер файла - " + maxFileSize);
    }

    @ExceptionHandler(FileAlreadyExistsException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public ErrorDto handleFileAlreadyExistException(FileAlreadyExistsException e) {
        log.error(e.getMessage());
        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDto handleException(Exception e) {
        log.error("Unhandled exception: ", e);
        return new ErrorDto("Возникла непредвиденная ошибка");
    }
}
