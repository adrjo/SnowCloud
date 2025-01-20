package com.github.adrjo.snowcloud.cloud;

import com.github.adrjo.snowcloud.auth.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class CloudService {
    private final CloudFileRepository fileRepository;
    private final CloudFolderRepository folderRepository;

    @Autowired
    public CloudService(CloudFileRepository fileRepository, CloudFolderRepository folderRepository) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
    }

    public List<FileMeta> getFiles(String directory, User user) throws FileNotFoundException {
        List<CloudFile> files = fileRepository.findFilesInDirectory(directory, user);
        if (files == null) {
            throw new FileNotFoundException("Directory not found");
        }
        List<FileMeta> meta = new ArrayList<>();
        for (CloudFile file : files) {
            meta.add(FileMeta.fromModel(file));
        }

        return meta;
    }

    public CloudFile getFile(String path, User user) throws FileNotFoundException {
        if (path.endsWith("/")) {
            throw new IllegalArgumentException("Downloading folder is disallowed.");
        }
        String directory = getDir(path);
        String fileName = (!directory.isEmpty() ? path.substring(directory.length() + 1) : path);

        Optional<CloudFile> file = fileRepository.findByDirectoryAndNameAndUser(directory, fileName, user);

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

    /**
     * Checks if folder already exists, makes sure that its parent exists, then creates the folder
     *
     * @param name of the folder
     * @param location of the folder
     * @param user the owner of the folder
     * @throws FileNotFoundException if the parent directory does not exist
     */
    public void createFolder(String name, String location, User user) throws FileNotFoundException {
        Optional<CloudFolder> folder = folderRepository.findByNameAndLocationAndUser(name, location, user);

        if (folder.isPresent()) {
            throw new IllegalArgumentException("Folder already exists.");
        }

        String[] parentParts = location.split("/");
        String parentName = parentParts[parentParts.length - 1];
        String parentLocation = String.join("", Arrays.copyOf(parentParts, parentParts.length - 1));

        Optional<CloudFolder> parent = folderRepository.findByNameAndLocationAndUser(parentName, parentLocation, user);

        if (parent.isEmpty()) {
            throw new FileNotFoundException("Parent folder does not exist!");
        }

        final CloudFolder newFolder = new CloudFolder(name, location, parent.get(), user);
        folderRepository.save(newFolder);
    }

    public void createRootDirectory(User user) {
        final CloudFolder root = new CloudFolder("", "", null, user);
        folderRepository.save(root);
    }
}
