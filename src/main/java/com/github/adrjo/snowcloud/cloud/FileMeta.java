package com.github.adrjo.snowcloud.cloud;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileMeta {
    private String name;
    private long size;
    private String contentType;
    private long lastModified;

    public static FileMeta fromModel(FileMetaProjection file) {
        return new FileMeta(file.getName(), file.getSize(), file.getContentType(), file.getLastModified());
    }

    public static FileMeta fromModel(CloudFile file) {
        return new FileMeta(file.getName(), file.getSize(), file.getContentType(), file.getLastModified());
    }

    public static FileMeta fromModel(CloudFolder folder) {
        return new FileMeta(folder.getName(), 0, "folder", -1);
    }
}
