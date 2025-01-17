package com.github.adrjo.snowcloud.cloud;

import com.github.adrjo.snowcloud.auth.User;
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

    // the directory the file is in
    private String directory;

    private byte[] fileData;

    @ManyToOne
    private User user;

    public CloudFile(String name, long size, String contentType, long lastModified, String directory) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.size = size;
        this.contentType = contentType;
        this.lastModified = lastModified;
        this.directory = directory;
        this.fileData = null;
    }
}
