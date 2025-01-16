package com.github.adrjo.snowcloud.cloud;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class CloudFile {

    @Id
    private final UUID id;

    private String name;
    private int size;
    private String contentType;
    private long lastModified;

    private byte[] fileData;

    public CloudFile(String name, int size, String contentType, long lastModified) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.size = size;
        this.contentType = contentType;
        this.lastModified = lastModified;
        this.fileData = null;
    }
}
