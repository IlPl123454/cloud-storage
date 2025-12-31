package com.plenkov.cloudstorage.controller.api;

import com.plenkov.cloudstorage.dto.ResourceDto;
import com.plenkov.cloudstorage.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Directory Controller", description = "Управление папками в облачном хранилище")
public interface DirectoryApi {
    @Operation(
            summary = "Получить содержимое папки",
            description = "Возвращает список всех файлов и подпапок по указанному пути пользователя"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Список ресурсов успешно получен",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResourceDto.class)))
    )
    @ApiResponse(responseCode = "404", description = "Директория не найдена")
    public List<ResourceDto> getDirectoryInfo(
            @Parameter(description = "Путь к папке (например, 'documents/work')", example = "photos")
            @RequestParam String path,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl user);


    @Operation(
            summary = "Создать пустую папку",
            description = "Создает новую папку по указанному пути"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Папка успешно создана",
            content = @Content(schema = @Schema(implementation = ResourceDto.class))
    )
    @ApiResponse(responseCode = "400", description = "Некорректный путь или папка уже существует")
    public ResourceDto createEmptyFolder(
            @Parameter(description = "Путь к новой папке (например, 'documents/work')", example = "photos/")
            @RequestParam String path,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl user);
}
