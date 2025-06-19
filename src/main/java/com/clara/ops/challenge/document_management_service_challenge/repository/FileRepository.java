package com.clara.ops.challenge.document_management_service_challenge.repository;

import com.clara.ops.challenge.document_management_service_challenge.entities.FileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;


@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    @Query("SELECT DISTINCT f FROM File f LEFT JOIN f.tags t " +
            "WHERE (:user IS NULL OR LOWER(f.user) LIKE LOWER(CONCAT('%', :user, '%'))) " +
            "AND (:name IS NULL OR LOWER(f.documentName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:tags IS NULL OR EXISTS (" +
            "  SELECT 1 FROM File doc JOIN doc.tags tag " +
            "  WHERE doc = d AND tag IN (:tags)))")
    Page<FileEntity> searchByFilters(@Param("user") String user,
                                     @Param("name") String name,
                                     @Param("tags") List<String> tags,
                                     Pageable pageable);
}