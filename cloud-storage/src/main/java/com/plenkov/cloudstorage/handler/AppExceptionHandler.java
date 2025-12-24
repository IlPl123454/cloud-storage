package com.plenkov.cloudstorage.handler;

import com.plenkov.cloudstorage.config.LogMessage;
import com.plenkov.cloudstorage.dto.ErrorDto;
import com.plenkov.cloudstorage.exception.*;
import com.plenkov.cloudstorage.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ErrorDto handleMethodArgumentNotValidException(BindingResult bindingResult) {
        String message = bindingResult.getAllErrors().getFirst().getDefaultMessage();
        log.error(message);
        return new ErrorDto(message);
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public ErrorDto handleUserAlreadyExistException(UserAlreadyExistException e) {
        log.error(String.format(LogMessage.EXCEPTION_USER_ALREADY_EXIST, e.getUsername()));
        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public ErrorDto handleIllegalArgument(IllegalArgumentException e,
                                          HttpServletRequest request,
                                          @AuthenticationPrincipal UserDetailsImpl user) {

        log.error(LogMessage.LOG_ILLEGAL_ARGUMENT,
                getUsername(user),
                getId(user),
                request.getRequestURI());

        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler(AuthException.class)
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ErrorDto handleAuthException(AuthException e) {
        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ErrorDto handleException(NoResourceFoundException e) {
        log.error(LogMessage.LOG_PAGE_NOT_FOUND, e.getMessage());
        return new ErrorDto(LogMessage.EXCEPTION_PAGE_NOT_FOUND);
    }


    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(value = HttpStatus.PAYLOAD_TOO_LARGE)
    public ErrorDto handleEMaxUploadSizeExceededException(MaxUploadSizeExceededException e,
                                                          HttpServletRequest request,
                                                          @AuthenticationPrincipal UserDetailsImpl user) {
        log.error(LogMessage.LOG_MAX_UPLOAD_SIZE,
                getUsername(user),
                getId(user),
                request.getRequestURI());

        return new ErrorDto(String.format(LogMessage.EXCEPTION_MAX_UPLOAD_SIZE, maxFileSize));
    }

    @ExceptionHandler(FileAlreadyExistsException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public ErrorDto handleFileAlreadyExistException(FileAlreadyExistsException e,
                                                    HttpServletRequest request,
                                                    @AuthenticationPrincipal UserDetailsImpl user) {
        log.warn(LogMessage.LOG_FILE_ALREADY_EXIST,
                getUsername(user),
                getId(user),
                e.getPath(),
                request.getRequestURI());

        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler(FileNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ErrorDto handleFileNotFound(FileNotFoundException e,
                                       HttpServletRequest request,
                                       @AuthenticationPrincipal UserDetailsImpl user) {
        log.warn(LogMessage.LOG_FILE_NOT_FOUND,
                getUsername(user),
                getId(user),
                e.getPath(),
                request.getRequestURI());

        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler(MinioStorageException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDto handleMinioStorageException(MinioStorageException e,
                                                HttpServletRequest request,
                                                @AuthenticationPrincipal UserDetailsImpl user) {
        log.error(LogMessage.LOG_MINIO_EXCEPTION,
                getUsername(user),
                getId(user),
                e.getPath(),
                request.getRequestURI());

        return new ErrorDto(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)

    public ErrorDto handleException(Exception e,
                                    HttpServletRequest request,
                                    @AuthenticationPrincipal UserDetailsImpl user) {

        log.error(LogMessage.LOG_UNHANDLED_EXCEPTION,
                getUsername(user),
                getId(user),
                request.getRequestURI(),
                e);

        return new ErrorDto(LogMessage.EXCEPTION_UNHANDLED_EXCEPTION);
    }


    private String getUsername(UserDetailsImpl user) {
        return user == null ? "not authorized" : user.getUsername();
    }

    private Long getId(UserDetailsImpl user) {
        return user == null ? 0 : user.getUserId();
    }
}