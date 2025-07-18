package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.entities.FileEntity;
import com.clara.ops.challenge.document_management_service_challenge.entities.TagEntity;
import com.clara.ops.challenge.document_management_service_challenge.repository.FileRepository;
import com.clara.ops.challenge.document_management_service_challenge.repository.FileSpecifications;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {
  private final FileRepository repository;
  private final MinioService minioService;
  private final ExecutorService executorService;

  public FileService(
      FileRepository repository, MinioService minioService, ExecutorService executorService) {
    this.repository = repository;
    this.minioService = minioService;
    this.executorService = executorService;
  }

  public CompletableFuture<FileEntity> upload(
      String user, String fileName, List<String> tags, MultipartFile file) throws Exception {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            if (repository.existsByUserNameAndFileName(user, fileName)) {
              throw new IllegalStateException(
                  "File with the same name already exists for this user.");
            }
            String minioPath = minioService.upload(fileName, file, user);

            List<TagEntity> tagEntities =
                tags.stream()
                    .map(tag -> new TagEntity(tag.trim().toLowerCase()))
                    .collect(Collectors.toList());

            FileEntity doc =
                FileEntity.builder()
                    .userName(user)
                    .fileName(fileName)
                    .tags(tagEntities)
                    .fileSize(file.getSize())
                    .fileType(file.getContentType())
                    .minioPath(minioPath)
                    .createdAt(Instant.now())
                    .build();

            return repository.save(doc);
          } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage(), e);
          }
        },
        executorService);
  }

  public Page<FileEntity> searchDocuments(
      String userName, String fileName, List<String> tags, Pageable pageable) {
    Specification<FileEntity> spec = FileSpecifications.withFilters(userName, fileName, tags);
    return repository.findAll(spec, pageable);
  }

  public Optional<String> generateDownloadUrl(Long id) throws Exception {
    return repository
        .findById(id)
        .map(FileEntity::getMinioPath)
        .map(
            path -> {
              try {
                return minioService.generateDownloadUrl(path);
              } catch (Exception e) {
                return null;
              }
            });
  }
}
