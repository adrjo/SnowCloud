package com.github.adrjo.snowcloud.cloud;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CloudRepository extends JpaRepository<CloudFile, UUID> {

    @Query("""
    SELECT new com.github.adrjo.snowcloud.cloud.CloudFile
    (file.name, file.size, file.contentType, file.lastModified, file.directory) FROM CloudFile file
    WHERE file.directory = :directory
    """)
    List<CloudFile> getFilesInDir(@Param("directory") String directory);

    Optional<CloudFile> findByDirectoryAndName(String directory, String name);
}
