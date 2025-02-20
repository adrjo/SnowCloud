package com.github.adrjo.snowcloud.cloud;

import com.github.adrjo.snowcloud.auth.User;
import com.github.adrjo.snowcloud.cloud.file.CloudFile;
import com.github.adrjo.snowcloud.cloud.file.CloudFileRepository;
import com.github.adrjo.snowcloud.cloud.file.FileMeta;
import com.github.adrjo.snowcloud.cloud.file.FileMetaProjection;
import com.github.adrjo.snowcloud.cloud.folder.CloudFolder;
import com.github.adrjo.snowcloud.cloud.folder.CloudFolderRepository;
import com.github.adrjo.snowcloud.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

@Service
public class CloudService {
    private final CloudFileRepository fileRepository;
    private final CloudFolderRepository folderRepository;

    @Autowired
    public CloudService(CloudFileRepository fileRepository, CloudFolderRepository folderRepository) {
        this.fileRepository = fileRepository;
        this.folderRepository = folderRepository;
    }


    /**
     * Gets meta-data for the files and folders in the folder located at the path specified.
     * No file or folder contents are sent here.
     *
     * @param path the full path to the folder
     * @param user the user sending the request
     * @return list of file and folder meta
     * @throws FileNotFoundException if the folder does not exist
     */
    public List<FileMeta> getFiles(String path, User user) throws FileNotFoundException {
        CloudFolder folder = fetchFolder(path, user)
                .orElseThrow(() -> new FileNotFoundException("Folder does not exist."));

        List<FileMetaProjection> files = fileRepository.findFileMetadataInFolder(folder);

        List<FileMeta> metas = new ArrayList<>();
        for (FileMetaProjection meta : files) {
            FileMeta f = FileMeta.fromModel(meta);
            f.addLink(user, path, false);

            metas.add(f);
        }

        for (CloudFolder subDir : folder.getFolders()) {
            FileMeta f = FileMeta.fromModel(subDir);
            f.addLink(user, path, true);

            metas.add(f);
        }

        return metas;
    }

    /**
     * Gets the full file data for the file located at the path specified.
     *
     * @param path the filepath
     * @param user the user sending the request
     * @return CloudFile file data
     * @throws FileNotFoundException if the folder or file does not exist.
     * @throws IllegalArgumentException if trying to download a folder
     */
    public CloudFile getFileData(String path, User user) throws FileNotFoundException {
        if (path.endsWith("/")) {
            throw new IllegalArgumentException("Downloading folder is disallowed.");
        }
        String location = getDir(path);

        CloudFolder folder = fetchFolder(location, user)
                .orElseThrow(() -> new FileNotFoundException("Folder does not exist."));

        String fileName = (!location.isEmpty() ? path.substring(location.length() + 1) : path);

        return fileRepository.findByFolderAndName(folder, fileName)
                .orElseThrow(() -> new FileNotFoundException("File not found"));
    }

    /**
     * Tries to create a new folder.
     *
     * @param name of the folder
     * @param location of the folder
     * @param user the owner of the folder
     * @throws FileNotFoundException if the parent folder does not exist
     * @throws IllegalArgumentException if the folder already exists
     */
    public CloudFolder createFolder(String name, String location, User user) throws FileNotFoundException {
        Optional<CloudFolder> folder = folderRepository.findByNameAndLocationAndUser(name, location, user);

        if (folder.isPresent()) {
            throw new IllegalArgumentException("Folder already exists.");
        }

        CloudFolder parent = fetchFolder(location, user)
                .orElseThrow(() -> new FileNotFoundException("Parent folder does not exist!"));

        final CloudFolder newFolder = new CloudFolder(name, location, parent, user);
        folderRepository.save(newFolder);

        return newFolder;
    }


    /**
     * Tries to upload a file
     *
     * @param uploadedFile the file to be uploaded
     * @param location folder in which the file should be uploaded
     * @param customName what name the file should use. if empty uses the original file name of the uploaded file
     * @param user the user uploading the file
     * @return FileMeta of the uploaded file
     * @throws IllegalArgumentException if the file has errors or already exists
     * @throws IOException if error in the file bytes
     */
    public FileMeta uploadFile(MultipartFile uploadedFile, String location, String customName, User user) throws IOException {
        if (uploadedFile == null) {
            throw new IllegalArgumentException("File may not be null.");
        }

        CloudFolder folder = fetchFolder(location, user)
                .orElseThrow(() -> new IllegalArgumentException("The folder at path: '" + location + "' does not exist."));

        String fileName = uploadedFile.getOriginalFilename();
        if (customName != null && !customName.isBlank()) {
            fileName = customName;
        }

        var fileExists = fileRepository.findByFolderAndName(folder, fileName);
        if (fileExists.isPresent()) {
            throw new IllegalArgumentException("'" + fileExists.get().getName() + "' already exists in this folder.");
        }

        final CloudFile file = new CloudFile(
                fileName,
                uploadedFile.getBytes(),
                uploadedFile.getSize(),
                uploadedFile.getContentType(),
                System.currentTimeMillis(),
                folder
        );

        fileRepository.save(file);
        return FileMeta.fromModel(file);
    }

    /**
     * Creates the original root folder for a user <br>
     * Called on user registration
     *
     * @param user the user to create the folder for.
     */
    public void createRootFolder(User user) {
        final CloudFolder root = new CloudFolder("", "", null, user);
        folderRepository.save(root);
    }

    /**
     * Tries to delete a file
     *
     * @param user the user sending the request
     * @param fileId of the file to be deleted
     * @throws FileNotFoundException if the file does not exist
     * @throws IllegalArgumentException if trying to delete other users files
     */
    public void deleteFile(User user, UUID fileId) throws FileNotFoundException {
        CloudFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found."));

        User fileOwner = file.getFolder().getUser();

        if (!fileOwner.getId().equals(user.getId())) {
            throw new IllegalArgumentException("Cannot delete other users files.");
        }

        fileRepository.delete(file);
    }

    /**
     * Tries to delete a folder
     *
     * @param user the user sending the request
     * @param id of the folder to be deleted
     * @throws FileNotFoundException if the folder does not exist
     * @throws IllegalArgumentException if trying to delete other users folder
     */
    public void deleteFolder(User user, UUID id) throws FileNotFoundException {
        CloudFolder folder = folderRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("Folder not found"));

        if (!folder.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Cannot delete other users folders.");
        }

        folderRepository.delete(folder); // folder contents should cascade delete automatically
    }

    /**
     * Helper function for getting a folder
     *
     * @param location of the folder
     * @param user the owner of the folder
     * @return CloudFolder
     */
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
        int finalFolderIndex = path.lastIndexOf('/');
        if (finalFolderIndex == -1) {
            return ""; // root folder
        }

        return path.substring(0, finalFolderIndex);
    }
}
