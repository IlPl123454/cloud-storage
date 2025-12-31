package com.plenkov.cloudstorage.controller;

import com.plenkov.cloudstorage.dto.ResourceDto;
import com.plenkov.cloudstorage.security.UserDetailsImpl;
import com.plenkov.cloudstorage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.plenkov.cloudstorage.controller.api.DirectoryApi;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController implements DirectoryApi {
    private final StorageService minioService;

    @Override
    @GetMapping
    public List<ResourceDto> getDirectoryInfo(@RequestParam String path, @AuthenticationPrincipal UserDetailsImpl user) {
        return minioService.getDirectoryInfo(path, user.getUserId());
    }

    @Override
    @PostMapping
    public ResourceDto createEmptyFolder(@RequestParam String path, @AuthenticationPrincipal UserDetailsImpl user) {
        return minioService.createEmptyFolder(path, user.getUserId());
    }
}