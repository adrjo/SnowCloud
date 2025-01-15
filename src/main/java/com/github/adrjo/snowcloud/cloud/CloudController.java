package com.github.adrjo.snowcloud.cloud;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
     * @param directory - the directory to use
     * @return list of FileMeta
     */
    @GetMapping("/files/{directory}")
    public ResponseEntity<List<FileMeta>> getFilesInDirectory(@PathVariable String directory) {
        try {
            List<FileMeta> files = service.getFiles(directory);

            return ResponseEntity.ok(files);
        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().build();
    }
        }

    @GetMapping("/download/{directory}/{fileName}")
    public ResponseEntity<File> downloadFile(@PathVariable String directory, @PathVariable String fileName) {
        try {
            File file = service.getFile(directory, fileName);

            return ResponseEntity.ok(file);
        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Data
    public static class FileDto {
        private String name;
        private int size;
        private byte[] data;
    }
}
