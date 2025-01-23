package com.github.adrjo.snowcloud.share;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShareRepository extends JpaRepository<TemporaryFile, UUID> {
}
