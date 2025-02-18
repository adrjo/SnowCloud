package com.github.adrjo.snowcloud.cloud.file;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.adrjo.snowcloud.cloud.folder.CloudFolder;
import com.github.adrjo.snowcloud.util.DateUtil;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"folder", "name"})
)
public class CloudFile {

    @Id
    private UUID id;

    private String name;
    private long size;
    private String contentType;
    private long lastModified;

    @ManyToOne
    @JsonIgnore
    private CloudFolder folder;

    private byte[] fileData;

    public CloudFile(String name, byte[] fileData, long size, String contentType, long lastModified, CloudFolder folder) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.size = size;
        this.contentType = contentType;
        this.lastModified = lastModified;
        this.folder = folder;
        this.fileData = fileData;
    }

    public String getLastModifiedFormatted() {
        return DateUtil.format(this.lastModified);
    }
}
