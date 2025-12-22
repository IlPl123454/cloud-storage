package com.plenkov.cloudstorage.mapper;

import com.plenkov.cloudstorage.dto.ResourceDto;
import com.plenkov.cloudstorage.util.MinioUtil;
import io.minio.ObjectWriteResponse;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;

public final class ResourceDtoMapper {
    public static ResourceDto toResourceDto(Item item) {
        ResourceDto.Type type = item.isDir() ? ResourceDto.Type.DIRECTORY : ResourceDto.Type.FILE;
        String path = item.objectName();

        return ResourceDto.builder()
                .name(MinioUtil.getName(path, item.isDir()))
                .size(item.size())
                .type(type)
                .path(MinioUtil.getPath(path, item.isDir()))
                .build();
    }

    public static ResourceDto toResourceDto(StatObjectResponse object) {

        boolean isDir;
        ResourceDto.Type type;

        if (object.object().lastIndexOf("/") == object.object().length() - 1) {
            type = ResourceDto.Type.DIRECTORY;
            isDir = true;
        } else {
            type = ResourceDto.Type.FILE;
            isDir = false;
        }

        String path = object.object();

        return ResourceDto.builder()
                .name(MinioUtil.getName(path, isDir))
                .size(object.size())
                .type(type)
                .path(MinioUtil.getPath(path, isDir))
                .build();
    }

    public static ResourceDto toResourceDto(ObjectWriteResponse object, Long size) {

        ResourceDto.Type type;
        boolean isDir;
        if (object.object().lastIndexOf("/") == object.object().length() - 1) {
            type = ResourceDto.Type.DIRECTORY;
            isDir = true;
        } else {
            type = ResourceDto.Type.FILE;
            isDir = false;
        }

        return ResourceDto.builder()
                .name(MinioUtil.getName(object.object(),isDir))
                .size(size)
                .type(type)
                .path(MinioUtil.getPath(object.object(),isDir))
                .build();
    }
}
