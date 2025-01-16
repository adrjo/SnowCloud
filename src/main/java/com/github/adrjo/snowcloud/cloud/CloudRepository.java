package com.github.adrjo.snowcloud.cloud;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CloudRepository extends JpaRepository<CloudFile, UUID> {

    @Query("SELECT new com.github.adrjo.snowcloud.cloud.CloudFile(file.name, file.size, file.contentType, file.lastModified) FROM CloudFile file")
    List<CloudFile> getFileMetaInDirectory();//todo dir
}

//```java
/// / Create a projection interface for the fields you want
//public interface FileMetaInfo {
//    String getName();
//    String getContentType();
//}
//
//@Repository
//public interface CloudFileRepository extends JpaRepository<CloudFile, UUID> {
//    @Query("SELECT c FROM CloudFile c")
//    List<FileMetaInfo> findAllBasicInfo();
//}
//```
