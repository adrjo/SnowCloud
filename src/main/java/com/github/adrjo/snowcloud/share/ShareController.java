package com.github.adrjo.snowcloud.share;

import com.github.adrjo.snowcloud.auth.User;
import com.github.adrjo.snowcloud.cloud.file.CloudFile;
import com.github.adrjo.snowcloud.util.Util;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.util.UUID;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
public class ShareController {

    private final ShareService service;

    @Autowired
    public ShareController(ShareService service) {
        this.service = service;
    }

    @PostMapping("/share")
    public ResponseEntity<?> shareFile(@AuthenticationPrincipal User user, @RequestBody ShareFileDto dto) {
        try {
            UUID shareToken = service.generateLink(user, dto.getFileId(), dto.getExpiryMinutes());

            return ResponseEntity.ok(GeneratedLinkDto.fromToken(shareToken));
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/share/{shareId}/revoke")
    public ResponseEntity<?> revokeSharedFile(@AuthenticationPrincipal User user, @PathVariable UUID shareId) {
        try {
            service.revoke(user, shareId);

            return ResponseEntity.ok("File share status revoked successfully");
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(e.getMessage());
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(FORBIDDEN)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/share/{shareId}")
    public ResponseEntity<?> getTemporaryFile(@PathVariable UUID shareId) {
        try {
            CloudFile file = service.getTemporaryFile(shareId);

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

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    public static class GeneratedLinkDto extends RepresentationModel<GeneratedLinkDto> {
        private UUID shareId;

        public static GeneratedLinkDto fromToken(UUID shareId) {
            var genned = new GeneratedLinkDto(shareId);
            Link revoke = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ShareController.class).revokeSharedFile(null, shareId))
                    .withRel("revoke");
            Link self = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ShareController.class).getTemporaryFile(shareId))
                    .withSelfRel();

            genned.add(revoke, self);
            return genned;
        }
    }
}
