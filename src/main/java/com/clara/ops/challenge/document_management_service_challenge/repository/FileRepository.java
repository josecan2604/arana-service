package com.clara.ops.challenge.document_management_service_challenge.repository;

import com.clara.ops.challenge.document_management_service_challenge.entities.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository
    extends JpaRepository<FileEntity, Long>, JpaSpecificationExecutor<FileEntity> {
        boolean existsByUserNameAndFileName(String userName, String fileName);


}
