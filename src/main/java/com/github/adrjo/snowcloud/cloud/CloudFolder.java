package com.github.adrjo.snowcloud.cloud;

import com.github.adrjo.snowcloud.auth.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "location", "user"})
)
public class CloudFolder {
    @Id
    private UUID id;

    private String name;
    private String location;

    // The files stored in this directory
    @OneToMany(mappedBy = "directory")
    private List<CloudFile> files;
    // The directories stored in this directory
    @OneToMany(mappedBy = "parent")
    private List<CloudFolder> directories;

    // The directory this directory is in, null if in the root directory
    @ManyToOne
    private CloudFolder parent;

    // Owner of the directory
    @ManyToOne
    private User user;

    public CloudFolder(String name, String location, CloudFolder parent, User user) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.location = location;
        this.files = new ArrayList<>();
        this.directories = new ArrayList<>();
        this.parent = parent;
        this.user = user;
    }
}
