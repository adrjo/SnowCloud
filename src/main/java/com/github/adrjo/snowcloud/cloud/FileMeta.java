package com.github.adrjo.snowcloud.cloud;

import lombok.Data;

@Data
public class FileMeta {
    private String name;
    private int size;
    private String contentType;
    private long lastModified;
}
