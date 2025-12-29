package com.plenkov.cloudstorage.service;

import com.plenkov.cloudstorage.config.LogMessage;
import com.plenkov.cloudstorage.dto.ResourceDto;
import com.plenkov.cloudstorage.exception.FileNotFoundException;
import com.plenkov.cloudstorage.exception.MinioStorageException;
import com.plenkov.cloudstorage.mapper.ResourceDtoMapper;
import com.plenkov.cloudstorage.util.MinioUtil;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
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
@Component
@RequiredArgsConstructor
public class MinioStorageProvider implements StorageProvider {
    private final MinioClient minioClient;
    @Value("${spring.minio.bucket-name}")
    private String bucket;

    @Override
    public ResourceDto getResourceInfo(String path) {
        try {
            StatObjectResponse file = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .build());

            return ResourceDtoMapper.toResourceDto(file);

        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                throw new FileNotFoundException(String.format(LogMessage.EXCEPTION_FILE_NOT_FOUND,
                        MinioUtil.getName(path, false)), path);
            } else {
                throw new MinioStorageException(LogMessage.EXCEPTION_MINIO_EXCEPTION, e, path);
            }
        } catch (IOException | ServerException | InsufficientDataException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new MinioStorageException(LogMessage.EXCEPTION_MINIO_EXCEPTION, e, path);
        }
    }

    @Override
    public List<ResourceDto> getDirectoryInfo(String path) {
        List<ResourceDto> resourceDto = new ArrayList<>();

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(path)
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
            throw new MinioStorageException(LogMessage.EXCEPTION_MINIO_EXCEPTION, e, path);
        }
    }

    @Override
    public ResourceDto uploadFile(MultipartFile file, String path) {
        try {
            ObjectWriteResponse object = minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build());

            return ResourceDtoMapper.toResourceDto(object, file.getSize());

        } catch (IOException | ServerException | InternalException | InvalidKeyException | InvalidResponseException |
                 NoSuchAlgorithmException | XmlParserException | ErrorResponseException | InsufficientDataException e) {
            throw new MinioStorageException(LogMessage.EXCEPTION_MINIO_EXCEPTION, e, path);
        }
    }

    @Override
    public InputStreamResource downloadFolder(String path) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zip = new ZipOutputStream(baos)) {
            Iterable<Result<Item>> items = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(path)
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
                                .bucket(bucket)
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
                throw new MinioStorageException((((ErrorResponseException) e).response().message()), e, path);
            }
            throw new MinioStorageException(LogMessage.EXCEPTION_MINIO_EXCEPTION, e, path);
        }
        return new InputStreamResource(new ByteArrayInputStream(baos.toByteArray()));
    }

    @Override
    public InputStreamResource downloadFile(String path) {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .build()
            );
            return new InputStreamResource(stream);

        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new MinioStorageException(LogMessage.EXCEPTION_MINIO_EXCEPTION, e, path);
        }
    }

    @Override
    public ResourceDto createEmptyFolder(String path) {
        try {
            ObjectWriteResponse object = minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .build());

            return ResourceDtoMapper.toResourceDto(object, 0L);

        } catch (IOException | ServerException | InternalException | InvalidKeyException | InvalidResponseException |
                 NoSuchAlgorithmException | XmlParserException | ErrorResponseException | InsufficientDataException e) {
            throw new MinioStorageException(LogMessage.EXCEPTION_MINIO_EXCEPTION, e, path);
        }
    }

    @Override
    public List<ResourceDto> searchByName(String userRootFolder, String query) {
        List<ResourceDto> resourceDto = new ArrayList<>();

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(userRootFolder)
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
            throw new MinioStorageException(LogMessage.EXCEPTION_MINIO_EXCEPTION, e, "");
        }
    }

    @Override
    public void deleteFolder(String path) {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(path)
                            .recursive(true)
                            .build());

            for (Result<Item> result : results) {
                deleteFile(result.get().objectName());
            }
        } catch (IllegalArgumentException | ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException |
                 ServerException | XmlParserException e) {
            throw new MinioStorageException(LogMessage.EXCEPTION_MINIO_EXCEPTION, e, path);
        }
    }

    @Override
    public void deleteFile(String path) {
        try {
            getResourceInfo(path);

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .build()
            );
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new MinioStorageException(LogMessage.EXCEPTION_MINIO_EXCEPTION, e, path);
        }
    }

    @Override
    public void copyFolder(String from, String to) {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(from)
                            .recursive(true)
                            .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                String fileFullSourceName = item.objectName();
                String fileTargetName = to + MinioUtil.getName(fileFullSourceName, item.isDir());

                copyFile(fileFullSourceName, fileTargetName);
            }

        } catch (IllegalArgumentException | ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | IOException | NoSuchAlgorithmException |
                 ServerException | XmlParserException e) {
            throw new MinioStorageException(LogMessage.EXCEPTION_MINIO_EXCEPTION, e, from);
        }
    }

    @Override
    public void copyFile(String from, String to) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucket)
                            .object(to)
                            .source(
                                    CopySource.builder()
                                            .bucket(bucket)
                                            .object(from)
                                            .build()
                            )
                            .build()
            );

        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new MinioStorageException(LogMessage.EXCEPTION_MINIO_EXCEPTION, e, from);
        }
    }
}
