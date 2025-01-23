package com.github.adrjo.snowcloud.cloud;

import java.util.UUID;

public interface FileMetaProjection {
    UUID getId();
    String getName();
    long getSize();
    String getContentType();
    long getLastModified();
}
