package com.plenkov.cloudstorage.service;

import com.plenkov.cloudstorage.dto.ResourceDto;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageProvider {
    ResourceDto getResourceInfo(String path);

    List<ResourceDto> getDirectoryInfo(String path);

    ResourceDto uploadFile(MultipartFile files, String path);

    InputStreamResource downloadFolder(String path);

    InputStreamResource downloadFile(String path);

    ResourceDto createEmptyFolder(String path);

    List<ResourceDto> searchByNane(String userRootFolder, String query);

    void deleteFolder(String path);

    void deleteFife(String path);

    void copyFolder(String from, String to);

    void copyFile(String from, String to);
}
