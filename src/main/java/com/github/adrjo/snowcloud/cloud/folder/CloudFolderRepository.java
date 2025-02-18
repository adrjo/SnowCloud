package com.github.adrjo.snowcloud.cloud.folder;

import com.github.adrjo.snowcloud.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CloudFolderRepository extends JpaRepository<CloudFolder, UUID> {

    Optional<CloudFolder> findByNameAndLocationAndUser(String name, String location, User user);
}
