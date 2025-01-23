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

    // The files stored in this folder
    @OneToMany(mappedBy = "folder", cascade = CascadeType.REMOVE)
    private List<CloudFile> files;
    // The folders stored in this folder
    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    private List<CloudFolder> folders;

    // The folder this folder is in, null if in the root folder
    @ManyToOne
    private CloudFolder parent;

    // Owner of the folder
    @ManyToOne
    private User user;

    public CloudFolder(String name, String location, CloudFolder parent, User user) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.location = location;
        this.files = new ArrayList<>();
        this.folders = new ArrayList<>();
        this.parent = parent;
        this.user = user;
    }
}
