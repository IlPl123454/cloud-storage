package com.plenkov.cloudstorage.util;

import io.minio.GenericResponse;
import io.minio.messages.Item;

public final class MinioUtil {
    public static String getName(Item item) {
        String path = item.objectName();

        if (item.isDir()) {
            path = path.substring(0, path.length() - 1);
        }

        int index = path.lastIndexOf('/');
        String name = path.substring(index + 1);

        return item.isDir() ? name + "/" : name;
    }

    public static String getPath(Item item) {
        String path = item.objectName();

        if (item.isDir()) {
            path = path.substring(0, path.length() - 1);
        }

        int index = path.lastIndexOf('/');

        return path.substring(0, index + 1);
    }

    public static String getName(GenericResponse object) {
        String path = object.object();
        boolean isDir = path.lastIndexOf('/') == path.length() - 1;

        if (isDir) {
            path = path.substring(0, path.length() - 1);
        }

        int index = path.lastIndexOf('/');
        String name = path.substring(index + 1);

        return isDir ? name + "/" : name;
    }

    public static String getPath(GenericResponse object) {
        String path = object.object();
        boolean isDir = path.lastIndexOf('/') == path.length() - 1;

        if (isDir) {
            path = path.substring(0, path.length() - 1);
        }

        int index = path.lastIndexOf('/');

        return path.substring(0, index + 1);
    }
}
