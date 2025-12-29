package com.plenkov.cloudstorage.util;

public final class MinioUtil {

    public static String getName(String path, boolean isDir) {
        if (isDir) {
            path = path.substring(0, path.length() - 1);
        }

        int index = path.lastIndexOf('/');
        String name = path.substring(index + 1);

        return isDir ? name + "/" : name;
    }

    public static String getPath(String path, boolean isDir) {
        if (isDir) {
            path = path.substring(0, path.length() - 1);
        }

        int index = path.indexOf("/");
        String name = path.substring(index + 1);

        index = name.lastIndexOf("/");
        if (index == -1) {
            return "";
        } else {
            name = name.substring(0, index);
        }

        return name + "/";
    }
}
