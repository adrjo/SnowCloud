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
        Optional<CloudFolder> folderOptional = fetchFolder(directory, user);

        if (folderOptional.isEmpty()) {
            throw new FileNotFoundException("Folder does not exist.");
        }

        CloudFolder folder = folderOptional.get();

        List<FileMetaProjection> files = fileRepository.findFileMetadataInFolder(folder);

        List<FileMeta> metas = new ArrayList<>();
        for (FileMetaProjection meta : files) {
            metas.add(FileMeta.fromModel(meta));
        }

        for (CloudFolder subDir : folder.getDirectories()) {
            metas.add(FileMeta.fromModel(subDir));
        }

        return metas;
    }

    public CloudFile getFileData(String path, User user) throws FileNotFoundException {
        if (path.endsWith("/")) {
            throw new IllegalArgumentException("Downloading folder is disallowed.");
        }
        String directory = getDir(path);

        Optional<CloudFolder> folder = fetchFolder(directory, user);
        if (folder.isEmpty()) {
            throw new FileNotFoundException("Folder does not exist.");
        }

        String fileName = (!directory.isEmpty() ? path.substring(directory.length() + 1) : path);

        Optional<CloudFile> file = fileRepository.findByDirectoryAndName(folder.get(), fileName);

        if (file.isEmpty()) {
            throw new FileNotFoundException("File not found");
        }

        return file.get();
    }

    /**
     * Checks if folder already exists, makes sure that its parent exists, then creates the folder
     *
     * @param name of the folder
     * @param location of the folder
     * @param user the owner of the folder
     * @throws FileNotFoundException if the parent directory does not exist
     */
    public CloudFolder createFolder(String name, String location, User user) throws FileNotFoundException {
        Optional<CloudFolder> folder = folderRepository.findByNameAndLocationAndUser(name, location, user);

        if (folder.isPresent()) {
            throw new IllegalArgumentException("Folder already exists.");
        }

        Optional<CloudFolder> parent = fetchFolder(location, user);

        if (parent.isEmpty()) {
            throw new FileNotFoundException("Parent folder does not exist!");
        }

        final CloudFolder newFolder = new CloudFolder(name, location, parent.get(), user);
        folderRepository.save(newFolder);

        return newFolder;
    }

    public void createRootDirectory(User user) {
        final CloudFolder root = new CloudFolder("", "", null, user);
        folderRepository.save(root);
    }

    private Optional<CloudFolder> fetchFolder(String location, User user) {
        String parentName = getFolderName(location);
        String parentLocation = getFolderLocation(location);

        return folderRepository.findByNameAndLocationAndUser(parentName, parentLocation, user);
    }

    private String getFolderName(String path) {
        String[] parentParts = path.split("/");
        return parentParts[parentParts.length - 1];
    }

    private String getFolderLocation(String path) {
        String[] parentParts = path.split("/");
        return String.join("", Arrays.copyOf(parentParts, parentParts.length - 1));
    }

    private String getDir(String path) {
        int finalDirectoryIndex = path.lastIndexOf('/');
        if (finalDirectoryIndex == -1) {
            return ""; // root directory
        }

        return path.substring(0, finalDirectoryIndex);
    }
}
