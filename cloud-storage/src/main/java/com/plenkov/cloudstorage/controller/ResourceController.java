package com.plenkov.cloudstorage.controller;

import com.plenkov.cloudstorage.dto.ResourceDto;
import com.plenkov.cloudstorage.security.UserDetailsImpl;
import com.plenkov.cloudstorage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/resource")
@RequiredArgsConstructor
public class ResourceController {
    private final StorageService minioService;

    @GetMapping("/search")
    @ResponseStatus(value = HttpStatus.OK)
    public List<ResourceDto> search(@RequestParam String query, @AuthenticationPrincipal UserDetailsImpl user) {
        return minioService.searchByNane(query, user.getUserId());
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    public ResourceDto getResource(@RequestParam String path, @AuthenticationPrincipal UserDetailsImpl user) {
        return minioService.getResourceInfo(path, user.getUserId());
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public List<ResourceDto> upload(MultipartFile[] object,
                                    @RequestParam String path,
                                    @AuthenticationPrincipal UserDetailsImpl user) {

        List<ResourceDto> resourceDto = minioService.uploadFile(object, path, user.getUserId());
        log.info("Загрузили файл{}", resourceDto.toString());
        return resourceDto;
    }

    @DeleteMapping
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void delete(@RequestParam String path, @AuthenticationPrincipal UserDetailsImpl user) {
        minioService.deleteResource(path, user.getUserId());
    }

    @GetMapping("/move")
    @ResponseStatus(value = HttpStatus.OK)
    public ResourceDto move(@RequestParam String from, @RequestParam String to, @AuthenticationPrincipal UserDetailsImpl user) {
        return minioService.moveResource(from, to, user.getUserId());
    }
}
