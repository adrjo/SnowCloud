package com.github.adrjo.snowcloud.share;

import com.github.adrjo.snowcloud.auth.User;
import com.github.adrjo.snowcloud.cloud.CloudFile;
import com.github.adrjo.snowcloud.cloud.CloudFileRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.Optional;
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

        Optional<CloudFile> fileOpt = fileRepository.findById(fileId);

        if (fileOpt.isEmpty()) {
            throw new FileNotFoundException("File not found.");
        }

        final CloudFile file = fileOpt.get();

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
     * @param tokenId the id for the shared file
     * @return CloudFile data
     * @throws FileNotFoundException if the file was not found in the database
     * @throws IllegalArgumentException if the file has expired
     */
    public CloudFile getTemporaryFile(UUID tokenId) throws FileNotFoundException {
        Optional<TemporaryFile> opt = shareRepository.findById(tokenId);

        if (opt.isEmpty()) {
            throw new FileNotFoundException("File expired or does not exist.");
        }

        TemporaryFile file = opt.get();

        if (System.currentTimeMillis() > file.getExpiresAt()) {
            shareRepository.delete(file);
            throw new IllegalArgumentException("File has expired!");
        }

        return file.getFile();
    }


    public String getUrlFromRequest(HttpServletRequest request) {
        String scheme = request.getScheme(); // http or https
        String serverName = request.getServerName(); // e.g., localhost or domain.com
        int serverPort = request.getServerPort(); // e.g., 8080
        String contextPath = request.getContextPath(); // e.g., /app

        if ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443)) {
            return String.format("%s://%s%s", scheme, serverName, contextPath);
        } else {
            return String.format("%s://%s:%d%s", scheme, serverName, serverPort, contextPath);
        }
    }
}
