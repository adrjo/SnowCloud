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
import java.util.List;

@RestController
public class CloudController {

    private final CloudService service;

    @Autowired
    public CloudController(CloudService service) {
        this.service = service;
    }

    /**
     * Get all the files in a user directory
     * Only file meta-data is sent here, no contents
     * to get file contents, use CloudController::downloadFile
     *
     * @param user the user sending the request
     * @param request
     *        to get the full directory path, including directories in directories, we use wildcard and
     *        HttpServletRequest to extract the full path after the /files/ endpoint.
     *        without this directories would not be able to be deeper than one.
     * @return list of FileMeta
     */
    @GetMapping("/files/**")
    public ResponseEntity<?> getFilesInDirectory(@AuthenticationPrincipal User user, HttpServletRequest request) {
        String directory = request.getRequestURI().substring("/files/".length());
        try {
            List<FileMeta> files = service.getFiles(directory, user);

            return ResponseEntity.ok(files);
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    /**
     * Returns the file in the user directory requested
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

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, file.getContentType())
                    .header(HttpHeaders.LAST_MODIFIED, file.getLastModifiedFormatted())
                    .body(file.getFileData()); // no content-length header needed, spring sets this automatically (and things break if set manually)
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
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
     * @param dto
     * @return status
     */
    @PostMapping("/create-folder")
    public ResponseEntity<?> createFolder(@AuthenticationPrincipal User user, @RequestBody CreateFolderDto dto) {
        try {
            CloudFolder folder = service.createFolder(dto.name, dto.location, user);

            return ResponseEntity.ok(CreateFolderResponseDto.fromModel(folder));
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/upload-file")
    public ResponseEntity<?> uploadFile(MultipartFile file) {
        throw new IllegalArgumentException("Not implemented");
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
