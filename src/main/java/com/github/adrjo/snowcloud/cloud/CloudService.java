package com.github.adrjo.snowcloud.cloud;

import com.github.adrjo.snowcloud.auth.User;
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<FileMeta> getFiles(String path, User user) throws FileNotFoundException {
        Optional<CloudFolder> folderOptional = fetchFolder(path, user);

        if (folderOptional.isEmpty()) {
            throw new FileNotFoundException("Folder does not exist.");
        }

        CloudFolder folder = folderOptional.get();

        List<FileMetaProjection> files = fileRepository.findFileMetadataInFolder(folder);

        List<FileMeta> metas = new ArrayList<>();
        for (FileMetaProjection meta : files) {
            metas.add(FileMeta.fromModel(meta));
        }

        for (CloudFolder subDir : folder.getFolders()) {
            metas.add(FileMeta.fromModel(subDir));
        }

        return metas;
    }

    public CloudFile getFileData(String path, User user) throws FileNotFoundException {
        if (path.endsWith("/")) {
            throw new IllegalArgumentException("Downloading folder is disallowed.");
        }
        String location = getDir(path);

        Optional<CloudFolder> folder = fetchFolder(location, user);
        if (folder.isEmpty()) {
            throw new FileNotFoundException("Folder does not exist.");
        }

        String fileName = (!location.isEmpty() ? path.substring(location.length() + 1) : path);

        Optional<CloudFile> file = fileRepository.findByFolderAndName(folder.get(), fileName);

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
     * @throws FileNotFoundException if the parent folder does not exist
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


    public FileMeta uploadFile(MultipartFile uploadedFile, String location, String customName, User user) throws IOException {
        if (uploadedFile == null) {
            throw new IllegalArgumentException("File may not be null.");
        }

        Optional<CloudFolder> folderOptional = fetchFolder(location, user);

        if (folderOptional.isEmpty()) {
            throw new IllegalArgumentException("The folder at path: '" + location + "' does not exist.");
        }

        String fileName = uploadedFile.getOriginalFilename();

        if (customName != null && !customName.isBlank()) {
            fileName = customName;
        }

        CloudFolder folder = folderOptional.get();

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

    public void createRootFolder(User user) {
        final CloudFolder root = new CloudFolder("", "", null, user);
        folderRepository.save(root);
    }

    private Optional<CloudFolder> fetchFolder(String location, User user) {
        String parentName = getFolderName(location);
        String parentLocation = getFolderLocation(location);

        return folderRepository.findByNameAndLocationAndUser(parentName, parentLocation, user);
    }

    public void deleteFile(User user, UUID fileId) throws FileNotFoundException {
        Optional<CloudFile> fileOpt = fileRepository.findById(fileId);

        if (fileOpt.isEmpty()) {
            throw new FileNotFoundException("File not found.");
        }

        final CloudFile file = fileOpt.get();

        User fileOwner = file.getFolder().getUser();

        if (!fileOwner.getId().equals(user.getId())) {
            throw new IllegalArgumentException("Cannot delete other users files.");
        }

        fileRepository.delete(file);
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
