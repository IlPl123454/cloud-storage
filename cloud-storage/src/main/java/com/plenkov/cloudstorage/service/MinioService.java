package com.plenkov.cloudstorage.service;

import com.plenkov.cloudstorage.dto.ResourceDto;
import com.plenkov.cloudstorage.exception.FileAlreadyExistsException;
import com.plenkov.cloudstorage.exception.MinioStorageException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {
    private final MinioClient minioClient;
    private final String bucketName = "user-files";

    public ResourceDto uploadFile(MultipartFile file, String path, Long id) {
        String userFolderName = getUserFolderName(id);
        String objectPath = userFolderName + "/" + path + "/" + file.getOriginalFilename();

        try {
            validateFileIsNotPresent(objectPath);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectPath)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build());
        } catch (IOException | ServerException | InternalException | InvalidKeyException | InvalidResponseException |
                 NoSuchAlgorithmException | XmlParserException | ErrorResponseException | InsufficientDataException e) {
            throw new MinioStorageException("Ошибка при попытке загрузить файл", e);
        }

        return ResourceDto.builder()
                .name(file.getOriginalFilename())
                .path(userFolderName + "/" + path)
                .type(ResourceDto.Type.FILE)
                .size(file.getSize())
                .build();
    }

    private void validateFileIsNotPresent(String path) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build());

            throw new FileAlreadyExistsException(path + " is already present");

        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                return;
            } else {
                throw new MinioStorageException("Не удалось прочитать данные о файле", e);
            }
        } catch (IOException | ServerException | InsufficientDataException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new MinioStorageException("Не удалось прочитать данные о файле", e);
        }
    }

    public void deleteFile(String path, Long id) {
        String userFolderName = getUserFolderName(id);

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            );
            log.info(path + " is deleted");
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RuntimeException(e);
        }
    }

    public void createUserFolder(Long id) {
        //TODO
    }

    public List<ResourceDto> getUserHomeDirectoryInfo(String path, Long userId)
            throws ServerException, InsufficientDataException, ErrorResponseException, IOException,
            NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException,
            InternalException {
        //TODO обработать исключения

        List<ResourceDto> resourceDto = new ArrayList<>();

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(getUserFolderName(userId) + "/" + path)
                        .recursive(false)
                        .build());

        for (Result<Item> result : results) {
            resourceDto.add(toResourceDto(result.get()));
        }
        return resourceDto;
    }

    private String getUserFolderName(Long userId) {
        return "user-" + userId + "-files";
    }


    private ResourceDto toResourceDto(Item item) {
        ResourceDto.Type type = item.isDir() ? ResourceDto.Type.DIRECTORY : ResourceDto.Type.FILE;

        return ResourceDto.builder()
                .name(getName(item))
                .size(item.size())
                .type(type)
                .path(getPath(item))
                .build();
    }

    private String getName(Item item) {
        String path = item.objectName();

        if (item.isDir()) {
            path = path.substring(0, path.length() - 1);
        }

        int index = path.lastIndexOf('/');
        String name = path.substring(index + 1);

        return item.isDir() ? name + "/" : name;
    }

    private String getPath(Item item) {
        String path = item.objectName();

        if (item.isDir()) {
            path = path.substring(0, path.length() - 1);
        }

        int index = path.lastIndexOf('/');

        return path.substring(0, index + 1);
    }
}


