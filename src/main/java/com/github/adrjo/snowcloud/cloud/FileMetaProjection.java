package com.github.adrjo.snowcloud.cloud;

public interface FileMetaProjection {
    String getName();
    long getSize();
    String getContentType();
    long getLastModified();
}
