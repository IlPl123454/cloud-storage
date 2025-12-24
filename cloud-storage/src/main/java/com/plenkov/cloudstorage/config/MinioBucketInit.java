package com.plenkov.cloudstorage.config;

import com.plenkov.cloudstorage.exception.MinioStorageException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioBucketInit {
    private final MinioClient minioClient;
    @Value("${spring.minio.bucket-name}")
    private String bucketName;

    @PostConstruct
    public void init() {

        if (!isBucketExists(bucketName)) {
            createBucket(bucketName);
        }
    }

    private boolean isBucketExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build());
            log.info("Bucket {} exists: {}", bucketName, exists);
            return exists;
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new MinioStorageException(LogMessage.EXCEPTION_MINIO_EXCEPTION, e, "");
        }
    }

    private void createBucket(String bucketName) {
        log.info("Creating {} bucket", bucketName);
        try {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new MinioStorageException(LogMessage.EXCEPTION_MINIO_EXCEPTION, e, "");

        }
    }
}
