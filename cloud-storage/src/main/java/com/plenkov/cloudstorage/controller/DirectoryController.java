package com.plenkov.cloudstorage.controller;

import com.plenkov.cloudstorage.dto.ResourceDto;
import com.plenkov.cloudstorage.security.UserDetailsImpl;
import com.plenkov.cloudstorage.service.MinioService;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {
    private final MinioService minioService;

    @GetMapping
    public List<ResourceDto> getDirectoryInfo(@RequestParam String path, @AuthenticationPrincipal UserDetailsImpl user)
            throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioService.getUserHomeDirectoryInfo(path, user.gerUserId());
    }
}
