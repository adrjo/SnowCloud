package com.github.adrjo.snowcloud.cloud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

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

    public CloudFile getFile(String directory, String fileName) throws FileNotFoundException {
        if (true) throw new FileNotFoundException("Directory not found");
        throw new FileNotFoundException("File not found");
    }
}
