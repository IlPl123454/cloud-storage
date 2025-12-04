package com.plenkov.cloudstorage.mapper;

import com.plenkov.cloudstorage.dto.ResourceDto;
import com.plenkov.cloudstorage.util.MinioUtil;
import io.minio.ObjectWriteResponse;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;

public final class ResourceDtoMapper {
    public static ResourceDto toResourceDto(Item item) {
        ResourceDto.Type type = item.isDir() ? ResourceDto.Type.DIRECTORY : ResourceDto.Type.FILE;

        return ResourceDto.builder()
                .name(MinioUtil.getName(item))
                .size(item.size())
                .type(type)
                .path(MinioUtil.getPath(item))
                .build();
    }

    public static ResourceDto toResourceDto(StatObjectResponse object) {

        ResourceDto.Type type;
        if (object.object().lastIndexOf("/") == object.object().length() - 1) {
            type = ResourceDto.Type.DIRECTORY;
        } else {
            type = ResourceDto.Type.FILE;
        }

        return ResourceDto.builder()
                .name(MinioUtil.getName(object))
                .size(object.size())
                .type(type)
                .path(MinioUtil.getPath(object))
                .build();
    }

    public static ResourceDto toResourceDto(ObjectWriteResponse object, Long size) {

        ResourceDto.Type type;
        if (object.object().lastIndexOf("/") == object.object().length() - 1) {
            type = ResourceDto.Type.DIRECTORY;
        } else {
            type = ResourceDto.Type.FILE;
        }

        return ResourceDto.builder()
                .name(MinioUtil.getName(object))
                .size(size)
                .type(type)
                .path(MinioUtil.getPath(object))
                .build();
    }
}
