package com.github.adrjo.snowcloud.cloud;

import com.github.adrjo.snowcloud.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CloudFileRepository extends JpaRepository<CloudFile, UUID> {

    @Query("""
    SELECT new com.github.adrjo.snowcloud.cloud.CloudFile
    (file.name, file.size, file.contentType, file.lastModified, file.directory, file.user) FROM CloudFile file
    WHERE file.directory = :directory AND file.user = :user
    """)
    List<CloudFile> findFilesInDirectory(@Param("directory") String directory, @Param("user") User user);

    Optional<CloudFile> findByDirectoryAndNameAndUser(String directory, String name, User user);
}
