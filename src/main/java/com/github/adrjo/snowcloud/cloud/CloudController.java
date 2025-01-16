package com.github.adrjo.snowcloud.cloud;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
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
     * @param directory - the directory to use
     * @return list of FileMeta
     */
    @GetMapping("/files/{directory}")
    public ResponseEntity<?> getFilesInDirectory(@PathVariable String directory) {
        try {
            List<CloudFile> files = service.getFiles(directory);
            List<FileMetaDto> response = new ArrayList<>();
            for (CloudFile file : files) {
                response.add(FileMetaDto.fromModel(file));
            }

            return ResponseEntity.ok(response);
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
    }
        }

    /**
     * Returns the file in the directory requested
     * @param directory directory to search in
     * @param fileName of the file
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

    @Data
    @AllArgsConstructor
    public static class FileMetaDto {
        private String name;
        private int size;
        private String contentType;
        private long lastModified;

        public static FileMetaDto fromModel(CloudFile file) {
            return new FileMetaDto(file.getName(), file.getSize(), file.getContentType(), file.getLastModified());
        }
    }
}
