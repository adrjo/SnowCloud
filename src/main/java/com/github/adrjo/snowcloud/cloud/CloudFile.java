package com.github.adrjo.snowcloud.cloud;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"directory", "name"})
)
public class CloudFile {

    @Id
    private UUID id;

    private String name;
    private long size;
    private String contentType;
    private long lastModified;

    @ManyToOne
    private CloudFolder directory;

    private byte[] fileData;

    public CloudFile(String name, long size, String contentType, long lastModified, CloudFolder directory) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.size = size;
        this.contentType = contentType;
        this.lastModified = lastModified;
        this.directory = directory;
        this.fileData = null;
    }
}
