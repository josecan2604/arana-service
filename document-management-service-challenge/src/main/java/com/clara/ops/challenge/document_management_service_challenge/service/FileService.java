package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.entities.File;
import com.clara.ops.challenge.document_management_service_challenge.repository.FileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {
    private final FileRepository repository;
    private final MinioService minioService;

    public FileService(FileRepository repository, MinioService minioService) {
        this.repository = repository;
        this.minioService = minioService;
    }

    public File upload(String user, String documentName, List<String> tags, MultipartFile file) throws Exception {
        String minioPath = minioService.upload(documentName, file, user);
        File doc = File.builder()
                .user(user)
                .documentName(documentName)
                .tags(tags)
                .fileSize(file.getSize())
                .fileType(file.getContentType())
                .minioPath(minioPath)
                .createdAt(Instant.now())
                .build();
        return repository.save(doc);
    }

    public Page<File> search(String user, String name, List<String> tags, Pageable pageable) {
        return    repository.searchByFilters(user, name, tags == null || tags.isEmpty() ? null : tags, pageable);

    }

    public Optional<String> generateDownloadUrl(Long id) throws Exception {
        return repository.findById(id)
                .map(File::getMinioPath)
                .map(path -> {
                    try {
                        return minioService.generateDownloadUrl(path);
                    } catch (Exception e) {
                        return null;
                    }
                });
    }
}
