package com.plenkov.cloudstorage.handler;

import com.plenkov.cloudstorage.dto.ErrorDto;
import com.plenkov.cloudstorage.exception.AuthException;
import com.plenkov.cloudstorage.exception.NotAuthorized;
import com.plenkov.cloudstorage.exception.UserAlreadyExistException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class AppExceptionHandler {
    @ExceptionHandler(UserAlreadyExistException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public ErrorDto handleUserAlreadyExistException(UserAlreadyExistException e) {
        log.error(e.getMessage());
        return new ErrorDto("Логин занят");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorDto handleMethodArgumentNotValidException(MethodArgumentNotValidException e, BindingResult bindingResult) {
        String message = bindingResult.getAllErrors().getFirst().getDefaultMessage();
        log.error(message);
        return new ErrorDto(message);
    }

    @ExceptionHandler(AuthException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ErrorDto handleAuthException(AuthException e) {
        log.error(e.getMessage());
        return new ErrorDto("Неверный логин или пароль");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDto handleException(NoResourceFoundException e) {
        log.error(e.getMessage());
        return new ErrorDto("404. Страница не найдена");
    }

    @ExceptionHandler(NotAuthorized.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ErrorDto handleNotAuthorized(NotAuthorized e) {
        log.error(e.getMessage());
        return new ErrorDto("Пользователь не авторизован.");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDto handleException(Exception e) {
        log.error("Unhandled exception: ", e);
        return new ErrorDto("Неизвестная ошибка");
    }
}
