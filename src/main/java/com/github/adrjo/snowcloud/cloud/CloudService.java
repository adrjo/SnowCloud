package com.github.adrjo.snowcloud.cloud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CloudService {
    private final CloudRepository repository;

    @Autowired
    public CloudService(CloudRepository repository) {
        this.repository = repository;
    }

    public List<FileMeta> getFiles(String directory) throws FileNotFoundException {
        List<CloudFile> files = repository.getFilesInDir(directory);
        if (files == null) {
            throw new FileNotFoundException("Directory not found");
        }
        List<FileMeta> meta = new ArrayList<>();
        for (CloudFile file : files) {
            meta.add(FileMeta.fromModel(file));
        }

        return meta;
    }

    public CloudFile getFile(String path) throws FileNotFoundException {
        if (path.endsWith("/")) {
            throw new IllegalArgumentException("Downloading folder is disallowed.");
        }
        String directory = getDir(path);
        String fileName = (!directory.isEmpty() ? path.substring(directory.length() + 1) : path);

        Optional<CloudFile> file = repository.findByDirectoryAndName(directory, fileName);

        if (file.isEmpty()) {
            throw new FileNotFoundException("File not found");
        }

        return file.get();
    }

    private String getDir(String path) {
        int finalDirectoryIndex = path.lastIndexOf('/');
        if (finalDirectoryIndex == -1) {
            return ""; // root directory
        }

        return path.substring(0, finalDirectoryIndex);
    }
}
