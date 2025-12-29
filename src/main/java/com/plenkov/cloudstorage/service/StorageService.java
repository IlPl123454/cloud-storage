package com.plenkov.cloudstorage.service;

import com.plenkov.cloudstorage.config.LogMessage;
import com.plenkov.cloudstorage.dto.ResourceDto;
import com.plenkov.cloudstorage.exception.FileAlreadyExistsException;
import com.plenkov.cloudstorage.exception.FileNotFoundException;
import com.plenkov.cloudstorage.util.MinioUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {
    private final StorageProvider storageProvider;

    public ResourceDto moveResource(String from, String to, Long id) {
        return from.endsWith("/") ? moveFolder(from, to, id) : moveFile(from, to, id);
    }

    private ResourceDto moveFile(String from, String to, Long id) {
        String fullSourcePath = getUserFolderName(id) + "/" + from;
        String fullTargetPath = getUserFolderName(id) + "/" + to;

        if (!isFileAlreadyExist(from, id)) {
            throw new FileNotFoundException(String.format(LogMessage.EXCEPTION_FILE_NOT_FOUND, from), fullSourcePath);
        }
        if (isFileAlreadyExist(to, id)) {
            throw new FileAlreadyExistsException(String.format(LogMessage.EXCEPTION_FILE_ALREADY_EXIST, to), fullTargetPath);
        }

        storageProvider.copyFile(fullSourcePath, fullTargetPath);
        storageProvider.deleteFile(fullSourcePath);

        return getResourceInfo(to, id);
    }

    private ResourceDto moveFolder(String from, String to, Long id) {
        String fullSourcePath = getUserFolderName(id) + "/" + from;
        String fullTargetPath = getUserFolderName(id) + "/" + to;

        //TODO проверить перемещение папки, которая уже есть

//        if (!isFileAlreadyExist(from, id)) {
//            throw new FileNotFoundException(from + " not found");
//        }
//        if (isFileAlreadyExist(to, id)) {
//            throw new FileAlreadyExistsException("Ресурс, уже находится в целевой папке");
//        }

        storageProvider.copyFolder(fullSourcePath, fullTargetPath);
        storageProvider.deleteFolder(fullSourcePath);

        return getResourceInfo(to, id);
    }

    public List<ResourceDto> searchByNane(String query, Long userId) {
        String userRoot = getUserFolderName(userId) + "/";
        return storageProvider.searchByName(userRoot, query);
    }

    public ResourceDto createEmptyFolder(String path, Long id) {
        String fullPath = getUserFolderName(id) + "/" + path;

        if (isFileAlreadyExist(path, id)) {
            throw new FileAlreadyExistsException(String.format(LogMessage.EXCEPTION_FILE_ALREADY_EXIST, path), fullPath);
        }
        return storageProvider.createEmptyFolder(fullPath);
    }

    public InputStreamResource download(String path, Long id) {
        return path.endsWith("/") ? downloadFolder(path, id) : downloadFile(path, id);
    }

    public InputStreamResource downloadFile(String path, Long id) {
        String fullPath = getUserFolderName(id) + "/" + path;
        return storageProvider.downloadFile(fullPath);
    }

    public InputStreamResource downloadFolder(String path, Long id) {
        String fullPath = getUserFolderName(id) + "/" + path;
        return storageProvider.downloadFolder(fullPath);
    }

    public List<ResourceDto> uploadFile(MultipartFile[] files, String path, Long id) {
        List<ResourceDto> resourceDtos = new ArrayList<>();
        String userFolderName = getUserFolderName(id);

        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            String fullPath;
            if (path.isEmpty()) {
                fullPath = userFolderName + "/" + fileName;
            } else {
                fullPath = userFolderName + "/" + path + "/" + fileName;
            }

            if (isFileAlreadyExist(path + fileName, id)) {
                throw new FileAlreadyExistsException(String.format(LogMessage.EXCEPTION_FILE_ALREADY_EXIST, path), fullPath);
            }

            ResourceDto resourceDto = storageProvider.uploadFile(file, fullPath);

            String emptyFolderFullName = MinioUtil.getPath(fullPath, false);
            if (!isFileAlreadyExist(emptyFolderFullName, id)) {
                createEmptyFolder(emptyFolderFullName, id);
            }

            resourceDtos.add(resourceDto);
        }
        return resourceDtos;
    }


    public void deleteResource(String path, Long id) {
        String fullPath = getUserFolderName(id) + "/" + path;

        if (path.endsWith("/")) {
            storageProvider.deleteFolder(fullPath);
        } else {
            storageProvider.deleteFile(fullPath);
        }
    }

    public List<ResourceDto> getDirectoryInfo(String path, Long userId) {
        String folderPath = getUserFolderName(userId) + "/" + path;
        return storageProvider.getDirectoryInfo(folderPath);
    }

    public ResourceDto getResourceInfo(String path, Long id) {
        String fullPath = getUserFolderName(id) + "/" + path;
        return storageProvider.getResourceInfo(fullPath);
    }

    private String getUserFolderName(Long userId) {
        return "user-" + userId + "-files";
    }

    private boolean isFileAlreadyExist(String path, Long id) {
        try {
            getResourceInfo(path, id);
        } catch (FileNotFoundException e) {
            return false;
        }
        return true;
    }
}


