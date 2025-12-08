package com.plenkov.cloudstorage.service;

import com.plenkov.cloudstorage.dto.ResourceDto;
import com.plenkov.cloudstorage.exception.FileAlreadyExistsException;
import com.plenkov.cloudstorage.exception.FileNotFoundException;
import com.plenkov.cloudstorage.exception.MinioStorageException;
import com.plenkov.cloudstorage.mapper.ResourceDtoMapper;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
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
    private final String MINIO_STORAGE_EXCEPTION_MESSAGE = "Не удалось прочитать данные о файле";

    public InputStreamResource downloadFile(String path) {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            );
            return new InputStreamResource(stream);

        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new MinioStorageException("Ошибка при скачивании файла", e);
        }
    }

    public ResourceDto getResourceInfo(String path) {
        try {
            StatObjectResponse file = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build());

            return ResourceDtoMapper.toResourceDto(file);

        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                throw new FileNotFoundException(path + " not found");
            } else {
                throw new MinioStorageException("Не удалось прочитать данные о файле", e);
            }
        } catch (IOException | ServerException | InsufficientDataException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new MinioStorageException("Не удалось прочитать данные о файле", e);
        }
    }

    public ResourceDto uploadFile(MultipartFile file, String path, Long id) {
        String userFolderName = getUserFolderName(id);

        String objectPath;
        if (path.isEmpty()) {
            objectPath = userFolderName + "/" + file.getOriginalFilename();
        } else {
            objectPath = userFolderName + "/" + path + "/" + file.getOriginalFilename();
        }

        if (isFileAlreadyExist(objectPath)) {
            throw new FileAlreadyExistsException(path + " уже существует");
        }

        try {
            ObjectWriteResponse object = minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectPath)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build());

            return ResourceDtoMapper.toResourceDto(object, file.getSize());

        } catch (IOException | ServerException | InternalException | InvalidKeyException | InvalidResponseException |
                 NoSuchAlgorithmException | XmlParserException | ErrorResponseException | InsufficientDataException e) {
            throw new MinioStorageException("Ошибка при попытке загрузить файл", e);
        }
    }

    public void deleteFile(String path) {
        try {
            getResourceInfo(path);

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
            throw new MinioStorageException("Не удалось удалить файл", e);
        }
    }


    public List<ResourceDto> getUserHomeDirectoryInfo(String path, Long userId) {

        List<ResourceDto> resourceDto = new ArrayList<>();

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(getUserFolderName(userId) + "/" + path)
                            .recursive(false)
                            .build());

            for (Result<Item> result : results) {
                resourceDto.add(ResourceDtoMapper.toResourceDto(result.get()));
            }
            return resourceDto;
        } catch (IllegalArgumentException | ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException |
                 ServerException | XmlParserException e) {
            throw new MinioStorageException("Не удалось получить содержимое папки", e);
        }
    }


    private String getUserFolderName(Long userId) {
        return "user-" + userId + "-files";
    }

    private boolean isFileAlreadyExist(String path) {
        try {
            getResourceInfo(path);
        } catch (FileNotFoundException e) {
            return false;
        }

        return true;
    }

}


