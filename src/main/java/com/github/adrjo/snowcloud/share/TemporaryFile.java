package com.github.adrjo.snowcloud.share;

import com.github.adrjo.snowcloud.cloud.file.CloudFile;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemporaryFile {

    @Id
    private UUID id;

    @OneToOne
    private CloudFile file;
    private long expiresAt;

    public TemporaryFile(CloudFile file, int expiryMinutes) {
        this.id = UUID.randomUUID();
        this.file = file;
        this.expiresAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(expiryMinutes);
    }
}
