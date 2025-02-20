package com.github.adrjo.snowcloud.share;

import com.github.adrjo.snowcloud.auth.User;
import com.github.adrjo.snowcloud.cloud.file.CloudFile;
import com.github.adrjo.snowcloud.cloud.file.CloudFileRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.UUID;

@Service
public class ShareService {

    private final ShareRepository shareRepository;
    private final CloudFileRepository fileRepository;

    @Autowired
    public ShareService(ShareRepository shareRepository, CloudFileRepository cloudFileRepository) {
        this.shareRepository = shareRepository;
        this.fileRepository = cloudFileRepository;
    }

    /**
     * Generates a temporary link to one of the user's files to be allowed to be shared without authorization.
     *
     * @param user the user sending the request
     * @param fileId the file-id of the file to-be shared
     * @param expiryMinutes expiry time in minutes for when the link should stop working
     * @return the generated share-token for the file
     * @throws FileNotFoundException if the fileId does not refer to an existing file
     * @throws IllegalArgumentException if invalid expiry-time or attempting to share other users files
     */
    public UUID generateLink(User user, UUID fileId, int expiryMinutes) throws FileNotFoundException {
        if (expiryMinutes <= 0) {
            throw new IllegalArgumentException("Expiry minutes must be a positive integer.");
        }

        CloudFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found."));

        User fileOwner = file.getFolder().getUser();

        if (!fileOwner.getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only share your own files!");
        }

        final TemporaryFile tempFile = new TemporaryFile(file, expiryMinutes);

        shareRepository.save(tempFile);
        return tempFile.getId();
    }

    /**
     * Tries to fetch a shared file
     *
     * @param shareId the id for the shared file
     * @return CloudFile data
     * @throws FileNotFoundException if the file was not found in the database
     * @throws IllegalArgumentException if the file has expired
     */
    public CloudFile getTemporaryFile(UUID shareId) throws FileNotFoundException {
        TemporaryFile file = shareRepository.findById(shareId)
                .orElseThrow(() -> new FileNotFoundException("File expired or does not exist."));

        if (System.currentTimeMillis() > file.getExpiresAt()) {
            shareRepository.delete(file);
            throw new IllegalArgumentException("File has expired!");
        }

        return file.getFile();
    }

    /**
     * Stops the sharing of a currently shared file
     *
     * @param user the user sending the request
     * @param shareId the shareId for the file to have its share status revoked
     * @throws FileNotFoundException if the file was not found in the database
     * @throws IllegalAccessException if trying to revoke share stats of another users file
     */
    public void revoke(User user, UUID shareId) throws FileNotFoundException, IllegalAccessException {
        TemporaryFile file = shareRepository.findById(shareId)
                .orElseThrow(() -> new FileNotFoundException("File expired or does not exist."));

        User fileOwner = file.getFile().getFolder().getUser();

        if (!fileOwner.getId().equals(user.getId())) {
            throw new IllegalAccessException("You can only revoke the share status of your own files!");
        }

        shareRepository.delete(file);
    }
}
