package com.plenkov.cloudstorage.service;

import com.plenkov.cloudstorage.dto.ResourceDto;
import com.plenkov.cloudstorage.exception.FileAlreadyExistsException;
import com.plenkov.cloudstorage.exception.FileNotFoundException;
import com.plenkov.cloudstorage.exception.MinioStorageException;
import com.plenkov.cloudstorage.mapper.ResourceDtoMapper;
import com.plenkov.cloudstorage.util.MinioUtil;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {
    private final MinioClient minioClient;
    private final String bucketName = "user-files";
    private final String MINIO_STORAGE_EXCEPTION_MESSAGE = "Не удалось прочитать данные о файле";

    public ResourceDto moveResource(String from, String to, Long id) {
        return from.endsWith("/") ? moveFolder(from, to, id) : moveFile(from, to, id);
    }

    private ResourceDto moveFile(String from, String to, Long id) {
        String fullSourcePath = getUserFolderName(id) + "/" + from;
        String fullTargetPath = getUserFolderName(id) + "/" + to;

        if (!isFileAlreadyExist(from, id)) {
            throw new FileNotFoundException(from + " not found");
        }
        ResourceDto sourceResourceDto = getResourceInfo(from, id);
        if (isFileAlreadyExist(to, id)) {
            throw new FileAlreadyExistsException("Ресурс, уже находится в целевой папке");
        }

        ObjectWriteResponse object;
        try {
            object = minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fullTargetPath)
                            .source(
                                    CopySource.builder()
                                            .bucket(bucketName)
                                            .object(fullSourcePath)
                                            .build()
                            )
                            .build()
            );

            deleteResource(from, id);

            return ResourceDtoMapper.toResourceDto(object, sourceResourceDto.getSize());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RuntimeException(e);
        }
    }

    private ResourceDto moveFolder(String from, String to, Long id) {
        String fullSourcePath = getUserFolderName(id) + "/" + from;

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(fullSourcePath)
                            .recursive(true)
                            .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                String fileFullSourceName = item.objectName();
                String fileSourceName = fileFullSourceName.substring(fileFullSourceName.indexOf('/') + 1);
                String fileTargetName = to + MinioUtil.getName(item);

                moveFile(fileSourceName, fileTargetName, id);
            }

            return getResourceInfo(to, id);

        } catch (IllegalArgumentException | ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException |
                 ServerException | XmlParserException e) {
            throw new MinioStorageException("Не удалось получить содержимое папки", e);
        }
    }

    public List<ResourceDto> searchByNane(String query, Long userId) {
        List<ResourceDto> resourceDto = new ArrayList<>();
        String userRoot = getUserFolderName(userId) + "/";

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(userRoot)
                            .recursive(true)
                            .build());

            for (Result<Item> result : results) {
                String objectName = MinioUtil.getName(result.get().objectName(), result.get().isDir());

                String fileName = objectName.substring(objectName.lastIndexOf("/") + 1);

                if (fileName.toLowerCase().contains(query.toLowerCase())) {
                    resourceDto.add(ResourceDtoMapper.toResourceDto(result.get()));
                }
            }

            return resourceDto;
        } catch (IllegalArgumentException | ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException |
                 ServerException | XmlParserException e) {
            throw new MinioStorageException("Не удалось получить содержимое папки", e);
        }
    }

    public ResourceDto createEmptyFolder(String path, Long id) {
        String fullPath = getUserFolderName(id) + "/" + path;

        if (isFileAlreadyExist(path, id)) {
            throw new FileAlreadyExistsException(path + " уже существует");
        }

        try {
            ObjectWriteResponse object = minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fullPath)
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .build());

            return ResourceDtoMapper.toResourceDto(object, 0L);

        } catch (IOException | ServerException | InternalException | InvalidKeyException | InvalidResponseException |
                 NoSuchAlgorithmException | XmlParserException | ErrorResponseException | InsufficientDataException e) {
            throw new MinioStorageException("Ошибка при попытке загрузить файл", e);
        }
    }

    public InputStreamResource download(String path, Long id) {
        return path.endsWith("/") ? downloadFolder(path, id) : downloadFile(path, id);
    }

    public InputStreamResource downloadFile(String path, Long id) {
        String fullPath = getUserFolderName(id) + "/" + path;

        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fullPath)
                            .build()
            );
            return new InputStreamResource(stream);

        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new MinioStorageException("Ошибка при скачивании", e);
        }
    }

    public InputStreamResource downloadFolder(String path, Long id) {
        String fullPath = getUserFolderName(id) + "/" + path;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zip = new ZipOutputStream(baos);) {
            Iterable<Result<Item>> items = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(fullPath)
                            .recursive(true)
                            .build());

            for (Result<Item> item : items) {
                String zipEntryName = item.get().objectName().substring(path.length());
                ZipEntry entry = new ZipEntry(zipEntryName);
                zip.putNextEntry(entry);

                String fileName = item.get().objectName();
                if (fileName.endsWith("/")) {
                    continue;
                }


                InputStream in = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fileName)
                                .build());

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    zip.write(buffer, 0, bytesRead);
                }

                zip.closeEntry();
            }

            zip.finish();

        } catch (IllegalArgumentException | ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException |
                 ServerException | XmlParserException e) {
            if (e instanceof ErrorResponseException) {
                throw new MinioStorageException((((ErrorResponseException) e).response().message()), e);
            }
            throw new MinioStorageException("Ошибка при скачивании", e);
        }

        return new InputStreamResource(new ByteArrayInputStream(baos.toByteArray()));
    }

    public List<ResourceDto> uploadFile(MultipartFile[] files, String path, Long id) {
        List<ResourceDto> resourceDtos = new ArrayList<>();
        String userFolderName = getUserFolderName(id);

        for (MultipartFile file : files) {
            String fullPath;
            if (path.isEmpty()) {
                fullPath = userFolderName + "/" + file.getOriginalFilename();
            } else {
                fullPath = userFolderName + "/" + path + "/" + file.getOriginalFilename();
            }

            if (isFileAlreadyExist(fullPath, id)) {
                throw new FileAlreadyExistsException(path + " уже существует");
            }


            try {
                ObjectWriteResponse object = minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fullPath)
                                .stream(file.getInputStream(), file.getSize(), -1)
                                .build());


                resourceDtos.add(ResourceDtoMapper.toResourceDto(object, file.getSize()));

            } catch (IOException | ServerException | InternalException | InvalidKeyException | InvalidResponseException |
                     NoSuchAlgorithmException | XmlParserException | ErrorResponseException | InsufficientDataException e) {
                throw new MinioStorageException("Ошибка при попытке загрузить файл", e);
            }
        }
        return resourceDtos;
    }



    public void deleteResource(String path, Long id) {
        if (path.endsWith("/")) {
            deleteFolder(path, id);
        } else {
            deleteFife(path, id);
        }
    }

    public void deleteFife(String path, Long id) {
        String fullPath = getUserFolderName(id) + "/" + path;
        try {
            getResourceInfo(path, id);

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fullPath)
                            .build()
            );
            log.info(path + " is deleted");
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new MinioStorageException("Не удалось удалить файл", e);
        }
    }

    public void deleteFolder(String path, Long id) {
        String fullSourcePath = getUserFolderName(id) + "/" + path;

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(fullSourcePath)
                            .recursive(true)
                            .build());

            for (Result<Item> result : results) {
                String fullFilePath = result.get().objectName();
                int index = fullFilePath.indexOf("/");

                String filePath = fullFilePath.substring(index + 1);

                deleteFife(filePath, id);
            }
        } catch (IllegalArgumentException | ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException |
                 ServerException | XmlParserException e) {
            throw new MinioStorageException("Не удалось получить содержимое папки", e);
        }
    }


    public List<ResourceDto> getDirectoryInfo(String path, Long userId) {
        String folderPath = getUserFolderName(userId) + "/" + path;
        List<ResourceDto> resourceDto = new ArrayList<>();

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(folderPath)
                            .recursive(false)
                            .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                if (item.size() > 0 || item.isDir()) {
                    resourceDto.add(ResourceDtoMapper.toResourceDto(item));
                }
            }
            return resourceDto;
        } catch (IllegalArgumentException | ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException |
                 ServerException | XmlParserException e) {
            throw new MinioStorageException("Не удалось получить содержимое папки", e);
        }
    }

    public ResourceDto getResourceInfo(String path, Long id) {
        String fullPath = getUserFolderName(id) + "/" + path;
        try {
            StatObjectResponse file = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fullPath)
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


