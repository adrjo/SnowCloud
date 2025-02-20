package com.github.adrjo.snowcloud.share;

import com.github.adrjo.snowcloud.auth.User;
import com.github.adrjo.snowcloud.cloud.file.CloudFile;
import com.github.adrjo.snowcloud.util.Util;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
public class ShareController {

    private final ShareService service;

    @Autowired
    public ShareController(ShareService service) {
        this.service = service;
    }

    @PostMapping("/generate-share-link")
    public ResponseEntity<?> shareFile(@AuthenticationPrincipal User user,
                                       @RequestBody ShareFileDto dto,
                                       HttpServletRequest request) {
        try {
            UUID shareToken = service.generateLink(user, dto.getFileId(), dto.getExpiryMinutes());

            return ResponseEntity.ok(GeneratedLinkDto.fromToken(shareToken, Util.getBaseUrl()));
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping("/share/{fileId}")
    public ResponseEntity<?> getTemporaryFile(@PathVariable UUID fileId) {
        try {
            CloudFile file = service.getTemporaryFile(fileId);

            boolean inline = Util.isInlineViewable(file.getContentType());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, (inline ? "inline" : "attachment") + "; filename=\"" + file.getName() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, file.getContentType())
                    .header(HttpHeaders.LAST_MODIFIED, file.getLastModifiedFormatted())
                    .body(file.getFileData()); // no content-length header needed, spring sets this automatically (and things break if set manually)
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @Data
    public static class ShareFileDto {
        private UUID fileId;
        private int expiryMinutes;
    }

    @Data
    @AllArgsConstructor
    public static class GeneratedLinkDto {
        private UUID shareToken;
        private String url;

        public static GeneratedLinkDto fromToken(UUID shareToken, String serverUrl) {
            return new GeneratedLinkDto(shareToken, serverUrl + "/share/" + shareToken);
        }
    }
}
