package com.github.adrjo.snowcloud.cloud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

@Service
public class CloudService {
    private final CloudRepository repository;

    @Autowired
    public CloudService(CloudRepository repository) {
        this.repository = repository;
    }

    public List<FileMeta> getFiles(String directory) throws FileNotFoundException {
        throw new FileNotFoundException("Directory not found");
    }

    public File getFile(String directory, String fileName) throws FileNotFoundException {
        if (true) throw new FileNotFoundException("Directory not found");
        throw new FileNotFoundException("File not found");
    }
}
