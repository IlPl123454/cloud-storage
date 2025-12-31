package com.plenkov.cloudstorage.controller.api;

import com.plenkov.cloudstorage.dto.ResourceDto;
import com.plenkov.cloudstorage.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Tag(name = "Resource Controller", description = "Управление файлами и папками (загрузка, поиск, удаление, перемещение)")
public interface ResourceApi {

    @Operation(summary = "Поиск файлов", description = "Ищет файлы по фрагменту имени")
    @ApiResponse(responseCode = "200", description = "Результаты поиска получены")
    List<ResourceDto> search(
            @Parameter(description = "Часть имени файла", example = "report") @RequestParam String query,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user
    );

    @Operation(summary = "Информация о ресурсе", description = "Получает метаданные файла или папки")
    @ApiResponse(responseCode = "200", description = "Метаданные получены")
    @ApiResponse(responseCode = "404", description = "Ресурс не найден")
    ResourceDto getResource(
            @Parameter(description = "Путь к ресурсу", example = "documents/cv.pdf") @RequestParam String path,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user
    );

    @Operation(
            summary = "Загрузка файлов",
            description = "Загружает один или несколько файлов в указанную директорию"
    )
    @ApiResponse(responseCode = "201", description = "Файлы успешно загружены")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    mediaType = MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(type = "object", requiredProperties = {"object", "path"}),
                    encoding = @io.swagger.v3.oas.annotations.media.Encoding(name = "object", contentType = "application/octet-stream")
            )
    )
    List<ResourceDto> upload(
            @Parameter(description = "Массив файлов для загрузки") MultipartFile[] object,
            @Parameter(description = "Целевой путь", example = "photos/summer") @RequestParam String path,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user
    );

    @Operation(summary = "Удаление ресурса", description = "Удаляет файл или всю папку рекурсивно")
    @ApiResponse(responseCode = "204", description = "Ресурс удален")
    void delete(
            @Parameter(description = "Путь к удаляемому ресурсу") @RequestParam String path,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user
    );

    @Operation(summary = "Перемещение/Переименование", description = "Перемещает ресурс по новому пути или переименовывает его")
    @ApiResponse(responseCode = "200", description = "Ресурс перемещен")
    ResourceDto move(
            @Parameter(description = "Откуда", example = "temp/file.txt") @RequestParam String from,
            @Parameter(description = "Куда", example = "work/file.txt") @RequestParam String to,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user
    );
}