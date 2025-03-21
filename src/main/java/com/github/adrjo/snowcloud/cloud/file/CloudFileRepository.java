package com.github.adrjo.snowcloud.cloud.file;

import com.github.adrjo.snowcloud.cloud.folder.CloudFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CloudFileRepository extends JpaRepository<CloudFile, UUID> {

    @Query("SELECT f.id as id, f.name AS name, f.size AS size, f.contentType AS contentType, f.lastModified AS lastModified " +
            "FROM CloudFile f WHERE f.folder = :folder")
    List<FileMetaProjection> findFileMetadataInFolder(@Param("folder") CloudFolder folder);

    Optional<CloudFile> findByFolderAndName(CloudFolder folder, String name);
}
