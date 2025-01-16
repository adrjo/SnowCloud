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

    // the directory the file is in
    private String directory;

    public static FileMeta fromModel(CloudFile file) {
        return new FileMeta(file.getName(), file.getSize(), file.getContentType(), file.getLastModified(), file.getDirectory());
    }
}
