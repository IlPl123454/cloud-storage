package com.plenkov.cloudstorage.controller;

import com.plenkov.cloudstorage.controller.api.DownloadApi;
import com.plenkov.cloudstorage.security.UserDetailsImpl;
import com.plenkov.cloudstorage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("api/resource/download")
@RequiredArgsConstructor
public class DownloadController implements DownloadApi {
    private final StorageService minioService;

    @Override
    @GetMapping(produces = "application/octet-stream")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public InputStreamResource download(@RequestParam String path,
                                        @AuthenticationPrincipal UserDetailsImpl user) {
        return minioService.download(path, user.getUserId());
    }
}
