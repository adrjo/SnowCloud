package com.github.adrjo.snowcloud.cloud;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
     * Get all the files in a directory
     * Only file meta-data is sent here, no contents
     * to get file contents, use CloudController::downloadFile
     *
     * @param request
     *        to get the full directory path, including directories in directories, we use wildcard and
     *        HttpServletRequest to extract the full path after the /files/ endpoint.
     *        without this directories would not be able to be deeper than one.
     * @return list of FileMeta
     */
    @GetMapping("/files/**")
    public ResponseEntity<?> getFilesInDirectory(HttpServletRequest request) {
        String directory = request.getRequestURI().substring("/files/".length());
        try {
            List<FileMeta> files = service.getFiles(directory);

            return ResponseEntity.ok(files);
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    /**
     * Returns the file in the directory requested
     *
     * @param directory directory to search in
     * @param fileName  of the file
     * @return file data
     */
    @GetMapping("/download/{directory}/{fileName}")
    public ResponseEntity<?> downloadFile(@PathVariable String directory, @PathVariable String fileName) {
        try {
            CloudFile file = service.getFile(directory, fileName);

            //todo
            return ResponseEntity.ok(file);
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}
