package com.plenkov.cloudstorage.service;

import com.plenkov.cloudstorage.dto.ResourceDto;
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

    public ResourceDto uploadFile(MultipartFile file, String path, Long id) throws Exception {
        String userFolderName = getUserFolderName(id);
        String objectPath = userFolderName + "/" + path + "/" + file.getOriginalFilename();

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket("user-files")
                            .object(objectPath)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build());
        } catch (IOException | ServerException | InternalException
                 | InvalidKeyException | InvalidResponseException
                 | NoSuchAlgorithmException | XmlParserException e) {
            throw new Exception(e);
        } catch (InsufficientDataException e) {
            throw new IllegalArgumentException(e);
        } catch (ErrorResponseException e) {
            throw new Exception(e);
        }


        return ResourceDto.builder()
                .name(file.getOriginalFilename())
                .path(userFolderName + "/" + path)
                .type(ResourceDto.Type.FILE)
                .size(file.getSize())
                .build();
    }

    public void deleteFile(String path, Long id) {
        String userFolderName = getUserFolderName(id);

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket("user-files")
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

        List<ResourceDto> resourceDto = new ArrayList<>();

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket("user-files")
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


