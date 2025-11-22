package com.plenkov.cloudstorage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceDto {
    private String name;
    private Type type;
    private String path;
    private Long size;

    public ResourceDto(String name, Type type, String path) {
        this.name = name;
        this.type = type;
        this.path = path;
    }

    public enum Type {
        FILE, DIRECTORY
    }
}
