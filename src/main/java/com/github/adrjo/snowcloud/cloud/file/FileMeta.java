package com.github.adrjo.snowcloud.cloud.file;

import com.github.adrjo.snowcloud.cloud.folder.CloudFolder;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import java.util.UUID;

@Data
@AllArgsConstructor
public class FileMeta extends RepresentationModel<FileMeta> {
    private UUID id;
    private String name;
    private long size;
    private String contentType;
    private long lastModified;

    public static FileMeta fromModel(FileMetaProjection file) {
        return new FileMeta(file.getId(),file.getName(), file.getSize(), file.getContentType(), file.getLastModified());
    }

    public static FileMeta fromModel(CloudFile file) {
        return new FileMeta(file.getId(), file.getName(), file.getSize(), file.getContentType(), file.getLastModified());
    }

    public static FileMeta fromModel(CloudFolder folder) {
        return new FileMeta(folder.getId(), folder.getName(), 0, "folder", -1);
    }
}
