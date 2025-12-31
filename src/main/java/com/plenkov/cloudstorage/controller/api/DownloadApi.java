package com.plenkov.cloudstorage.controller.api;

import com.plenkov.cloudstorage.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Download Controller", description = "Операции по скачиванию файлов")

public interface DownloadApi {
    @Operation(
            summary = "Скачать файл",
            description = "Позволяет скачать файл из хранилища по указанному пути. Возвращает поток байт (octet-stream)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Файл успешно получен",
            content = @Content(
                    mediaType = "application/octet-stream",
                    schema = @Schema(type = "string", format = "binary")
            )
    )
    @ApiResponse(responseCode = "404", description = "Файл не найден")
    @ApiResponse(responseCode = "403", description = "Нет доступа к файлу")
    public InputStreamResource download(
            @Parameter(description = "Полный путь к файлу в хранилище", example = "documents/contract.pdf")
            @RequestParam String path,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl user);
}
