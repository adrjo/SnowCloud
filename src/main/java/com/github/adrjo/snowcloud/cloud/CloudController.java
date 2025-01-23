package com.github.adrjo.snowcloud.cloud;

import com.github.adrjo.snowcloud.auth.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
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
     * Get all the files in a user folder
     * Only file meta-data is sent here, no contents
     * to get file contents, use CloudController::downloadFile
     *
     * @param user the user sending the request
     * @param request
     *        to get the full folder path, including folders in folders, we use wildcard and
     *        HttpServletRequest to extract the full path after the /files/ endpoint.
     *        without this folders would not be able to be deeper than one.
     * @return list of FileMeta
     */
    @GetMapping("/files/**")
    public ResponseEntity<?> getFilesInFolder(@AuthenticationPrincipal User user, HttpServletRequest request) {
        String path = request.getRequestURI().substring("/files/".length());
        try {
            List<FileMeta> files = service.getFiles(path, user);

            return ResponseEntity.ok(files);
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    /**
     * Returns the file in the user folder requested
     *
     * @param user the user sending the request
     * @param request full path of the file to be downloaded
     * @return file data
     */
    @GetMapping("/download/**")
    public ResponseEntity<?> downloadFile(@AuthenticationPrincipal User user, HttpServletRequest request) {
        String path = request.getRequestURI().substring("/download/".length());
        try {
            CloudFile file = service.getFileData(path, user);

            boolean inline = isInlineViewable(file.getContentType());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, (inline ? "inline" : "attachment") + "; filename=\"" + file.getName() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, file.getContentType())
                    .header(HttpHeaders.LAST_MODIFIED, file.getLastModifiedFormatted())
                    .body(file.getFileData()); // no content-length header needed, spring sets this automatically (and things break if set manually)
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    /**
     * Whether the file should be viewable in the browser or directly downloaded
     * MIGHT BE DANGEROUS, limited to only a few formats for now
     *
     * @param contentType the content type of the file
     * @return true if the file should be viewable in browser, false otherwise
     */
    private boolean isInlineViewable(String contentType) {
        return contentType.startsWith("image/")
                || contentType.equals("application/pdf")
                || contentType.equals("text/plain");
    }

    /**
     * Creates a folder
     * location needs to point to a folder that already exists, or be left empty (root folder)
     *
     * @param user the user sending the request
     * @param dto name and path location of where the folder should be created
     * @return folder info on success
     */
    @PostMapping("/create-folder")
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

    @PostMapping("/upload-file")
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


    @DeleteMapping("/delete-file")
    public ResponseEntity<?> deleteFile(@AuthenticationPrincipal User user, @RequestBody GenericDeleteByIdDto dto) {
        try {
            service.deleteFile(user, dto.getId());

            return ResponseEntity.ok("Success");
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(FORBIDDEN)
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/delete-folder")
    public ResponseEntity<?> deleteFolder(@AuthenticationPrincipal User user, @RequestBody GenericDeleteByIdDto dto) {
        try {
            service.deleteFolder(user, dto.getId());

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
    public static class GenericDeleteByIdDto {
        private UUID id;
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
