package com.plenkov.cloudstorage.controller;

import com.plenkov.cloudstorage.dto.ResourceDto;
import com.plenkov.cloudstorage.security.UserDetailsImpl;
import com.plenkov.cloudstorage.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("api/resource")
@RequiredArgsConstructor
public class ResourceController {
    private final MinioService minioService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public ResourceDto upload(MultipartFile object,
                              @RequestParam String path,
                              @AuthenticationPrincipal UserDetailsImpl user) {

        ResourceDto resourceDto = minioService.uploadFile(object, path, user.getUserId());
        log.info("Загрузили файл{}", resourceDto.toString());
        return resourceDto;
    }

    @DeleteMapping
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void delete(@RequestParam String path, @AuthenticationPrincipal UserDetailsImpl user) {
        minioService.deleteFile(path, user.getUserId());
    }
}
