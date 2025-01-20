package com.github.adrjo.snowcloud.cloud;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CloudFileRepository extends JpaRepository<CloudFile, UUID> {

    @Query("SELECT f.name AS name, f.size AS size, f.contentType AS contentType, f.lastModified AS lastModified " +
            "FROM CloudFile f WHERE f.directory = :folder")
    List<FileMetaProjection> findFileMetadataInFolder(@Param("folder") CloudFolder folder);

    Optional<CloudFile> findByDirectoryAndName(CloudFolder directory, String name);
}
