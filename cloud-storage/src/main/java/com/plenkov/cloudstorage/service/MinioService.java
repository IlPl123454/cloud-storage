package com.plenkov.cloudstorage.service;

import com.plenkov.cloudstorage.dto.ResourceDto;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public void createUserFolder(Long id) {
    //TODO
    }

    public List<ResourceDto> getUserHomeDirectoryInfo(String path, Long userId)
            throws ServerException, InsufficientDataException, ErrorResponseException, IOException,
            NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException,
            InternalException {

        List<ResourceDto> resourceDto = new ArrayList<>();
        String userFolder = "user-" + userId + "-files";

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket("user-files")
                        .prefix(userFolder + "/" + path)
                        .recursive(false)
                        .build());

        for (Result<Item> result : results) {
            resourceDto.add(toResourceDto(result.get()));
            log.info(result.get().objectName());
        }
        return resourceDto;
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


