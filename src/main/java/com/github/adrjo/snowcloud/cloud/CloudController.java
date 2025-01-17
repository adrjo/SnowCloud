package com.github.adrjo.snowcloud.cloud;

import com.github.adrjo.snowcloud.auth.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
            CloudFile file = service.getFile(path, user);

            //todo
            return ResponseEntity.ok(file);
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}
