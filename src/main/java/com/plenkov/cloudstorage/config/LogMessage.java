package com.plenkov.cloudstorage.config;

public class LogMessage {

    public static final String LOG_FILE_ALREADY_EXIST = "Юзер {} (id={}) пытается загрузить файл который  уже существует. Путь: {}. URL: {}";
    public static final String EXCEPTION_FILE_ALREADY_EXIST = "Файл уже существует - %s";

    public static final String LOG_FILE_NOT_FOUND = "Юзер {} (id={}) выполнить действие с файлом которого не существует. Путь: {}. URL: {}";
    public static final String EXCEPTION_FILE_NOT_FOUND = "Файл не найден - %s";

    public static final String LOG_MINIO_EXCEPTION = "Возникла ошибка при работе с Minio. Юзер {} (id={}) Путь: {}. URL: {}";
    public static final String EXCEPTION_MINIO_EXCEPTION = "Возникла ошибка при работе с Minio";

    public static final String LOG_UNHANDLED_EXCEPTION  = "Возникла непредвиденная ошибка. Юзер {} (id={}). URL: {}";
    public static final String EXCEPTION_UNHANDLED_EXCEPTION = "Возникла непредвиденная ошибка";


    public static final String LOG_ILLEGAL_ARGUMENT = "Невалидное тело запроса. Юзер {} (id={}). URL: {}";
    public static final String EXCEPTION_ILLEGAL_ARGUMENT = "Невалидное тело запроса";

    public static final String EXCEPTION_USER_ALREADY_EXIST = "Юзер '%s' уже существует";

    public static final String EXCEPTION_FAILED_AUTHENTICATION = "Неверный логин или пароль";

    public static final String LOG_PAGE_NOT_FOUND = "404. Страница не найдена. Путь - {}";
    public static final String EXCEPTION_PAGE_NOT_FOUND = "404. Страница не найдена";

    public static final String LOG_MAX_UPLOAD_SIZE = "Юзер {} (id={}) пытается загрузить слишком большой файл. URL: {}\"";
    public static final String EXCEPTION_MAX_UPLOAD_SIZE = "Максимальный размер файла - %s";

}
