package com.github.adrjo.snowcloud.cloud.file;

import com.github.adrjo.snowcloud.cloud.CloudController;
import com.github.adrjo.snowcloud.cloud.folder.CloudFolder;
import com.github.adrjo.snowcloud.share.ShareController;
import com.github.adrjo.snowcloud.util.Util;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
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

    public void addLink(String path, boolean folder) {
        Link del = WebMvcLinkBuilder
                .linkTo(folder
                        ? WebMvcLinkBuilder.methodOn(CloudController.class)
                                .deleteFolder(null, this.getId())
                        : WebMvcLinkBuilder.methodOn(CloudController.class)
                                .deleteFile(null, this.getId())
                        )
                .withRel("delete");

        Link self = WebMvcLinkBuilder
                .linkTo(WebMvcLinkBuilder.methodOn(CloudController.class)
                        .viewFileOrFolder(null, null))
                .withSelfRel()
                .withHref(Util.getBaseUrl() + "/files/" + path + this.getName() + (folder ? "/" : ""));

        if (!folder) {
            Link share = WebMvcLinkBuilder
                    .linkTo(WebMvcLinkBuilder.methodOn(ShareController.class)
                            .shareFileTemporarily(null, this.getId()))
                    .withRel("share");
            this.add(share);
        }

        this.add(del, self);
    }
}
