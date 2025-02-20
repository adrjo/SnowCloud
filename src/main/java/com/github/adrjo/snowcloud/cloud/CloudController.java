package com.github.adrjo.snowcloud.cloud;

import com.github.adrjo.snowcloud.auth.User;
import com.github.adrjo.snowcloud.cloud.file.CloudFile;
import com.github.adrjo.snowcloud.cloud.file.FileMeta;
import com.github.adrjo.snowcloud.cloud.folder.CloudFolder;
import com.github.adrjo.snowcloud.util.Util;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
public class CloudController {

    private final CloudService service;

    @Autowired
    public CloudController(CloudService service) {
        this.service = service;
    }

    /**
     * View the file or folder requested <br>
     * If the path ends in a "/" the meta information for all the files in the folder is returned <br>
     * Otherwise the file is returned, including the file data
     *
     * @param user    the user sending the request
     * @param request the raw http request, used for parsing the full path
     * @return list of FileMeta or a single file's content
     */
    @GetMapping("/files/**")
    public ResponseEntity<?> viewFileOrFolder(@AuthenticationPrincipal User user, HttpServletRequest request) {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
        String path = request.getRequestURI().substring("/files/".length());

        String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
        try {
            if (!decodedPath.isBlank() && !decodedPath.endsWith("/")) {
                CloudFile file = service.getFileData(decodedPath, user);

                boolean inline = Util.isInlineViewable(file.getContentType());

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, (inline ? "inline" : "attachment") + "; filename=\"" + file.getName() + "\"")
                        .header(HttpHeaders.CONTENT_TYPE, file.getContentType())
                        .header(HttpHeaders.LAST_MODIFIED, file.getLastModifiedFormatted())
                        .body(file.getFileData()); // no content-length header needed, spring sets this automatically (and things break if set manually)
            }

            List<FileMeta> files = service.getFiles(decodedPath, user);
            List<EntityModel<FileMeta>> filesWithLinks = new ArrayList<>();
            for (FileMeta f : files) {
                boolean folder = f.getLastModified() == -1;
                EntityModel<FileMeta> model = EntityModel.of(f);

                Link del = WebMvcLinkBuilder.linkTo(folder
                                ? WebMvcLinkBuilder.methodOn(this.getClass()).deleteFolder(user, f.getId())
                                : WebMvcLinkBuilder.methodOn(this.getClass()).deleteFile(user, f.getId())
                        )
                        .withRel("delete");
                Link self = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).viewFileOrFolder(user, null))
                        .withSelfRel()
                        .withHref(baseUrl + "/files/" + path + f.getName() + (folder ? "/" : ""));
                model.add(del, self);
                filesWithLinks.add(model);
            }

            return ResponseEntity.ok(filesWithLinks);
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    /**
     * Creates a folder
     * location needs to point to a folder that already exists, or be left empty (root folder)
     *
     * @param user the user sending the request
     * @param dto  name and path location of where the folder should be created
     * @return folder info on success
     */
    @PostMapping("/folders")
    public ResponseEntity<?> createFolder(@AuthenticationPrincipal User user, @RequestBody CreateFolderDto dto) {
        try {
            CloudFolder folder = service.createFolder(dto.name, dto.location, user);

            return ResponseEntity.ok(CreateFolderResponseDto.fromModel(folder));
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/files")
    public ResponseEntity<?> uploadFile(@AuthenticationPrincipal User user,
                                        @RequestParam("file") MultipartFile file,
                                        @RequestParam("location") String location,
                                        @RequestParam(value = "customName", required = false) String customName) {
        try {
            FileMeta meta = service.uploadFile(file, location, customName, user);

            return ResponseEntity.ok(meta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid file data");
        }
    }


    @DeleteMapping("/files/{id}")
    public ResponseEntity<?> deleteFile(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        try {
            service.deleteFile(user, id);

            return ResponseEntity.ok("Success");
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(FORBIDDEN)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/folders/{id}")
    public ResponseEntity<?> deleteFolder(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        try {
            service.deleteFolder(user, id);

            return ResponseEntity.ok("Success");
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(FORBIDDEN)
                    .body(e.getMessage());
        }
    }

    @Data
    public static class CreateFolderDto {
        private String name;
        private String location;
    }

    @Data
    @AllArgsConstructor
    public static class CreateFolderResponseDto {
        private String name;
        private String location;
        private String user;

        public static CreateFolderResponseDto fromModel(CloudFolder folder) {
            return new CreateFolderResponseDto(folder.getName(), folder.getLocation(), folder.getUser().getUsername());
        }
    }
}
