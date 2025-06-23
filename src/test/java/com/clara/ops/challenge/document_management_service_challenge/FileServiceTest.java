package com.clara.ops.challenge.document_management_service_challenge;

import com.clara.ops.challenge.document_management_service_challenge.entities.FileEntity;
import com.clara.ops.challenge.document_management_service_challenge.repository.FileRepository;
import com.clara.ops.challenge.document_management_service_challenge.service.FileService;
import com.clara.ops.challenge.document_management_service_challenge.service.MinioService;
import com.clara.ops.challenge.document_management_service_challenge.repository.FileSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;


import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileServiceTest {

    @Mock private FileRepository repository;
    @Mock private MinioService minioService;
    @Mock private ExecutorService executorService;
    @Mock private MultipartFile multipartFile;

    @InjectMocks private FileService fileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        executorService = Executors.newSingleThreadExecutor(); // Real executor for tests
        fileService = new FileService(repository, minioService, executorService);
    }

    @Test
    void upload_shouldSaveDocumentAndReturnFileEntity() throws Exception {
        // Given
        String user = "testuser";
        String docName = "doc.pdf";
        List<String> tags = List.of("TagOne", "TagTwo");

        when(minioService.upload(eq(docName), any(MultipartFile.class), eq(user)))
                .thenReturn("file-bucket/testuser/doc.pdf");

        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getContentType()).thenReturn("application/pdf");

        ArgumentCaptor<FileEntity> captor = ArgumentCaptor.forClass(FileEntity.class);
        FileEntity savedEntity = FileEntity.builder().id(1L).fileName(docName).build();
        when(repository.save(any(FileEntity.class))).thenReturn(savedEntity);

        // When
        CompletableFuture<FileEntity> future =
                fileService.upload(user, docName, tags, multipartFile);
        FileEntity result = future.get(2, TimeUnit.SECONDS);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFileName()).isEqualTo(docName);
        verify(repository).save(captor.capture());
        FileEntity captured = captor.getValue();
        assertThat(captured.getTags()).hasSize(2);
        assertThat(captured.getFileSize()).isEqualTo(1024L);
        assertThat(captured.getFileType()).isEqualTo("application/pdf");
    }

    @Test
    void upload_shouldThrowRuntimeExceptionOnFailure() throws Exception {
        when(minioService.upload(any(), any(), any())).thenThrow(new RuntimeException("fail"));
        when(multipartFile.getSize()).thenReturn(0L);
        when(multipartFile.getContentType()).thenReturn("application/pdf");

        CompletableFuture<FileEntity> future =
                fileService.upload("user", "file", List.of(), multipartFile);

        assertThatThrownBy(future::join)
                .hasCauseInstanceOf(RuntimeException.class)
                .hasRootCauseMessage("fail");
    }

    @Test
    void searchDocuments_shouldReturnFilteredPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<FileEntity> page = new PageImpl<>(List.of(FileEntity.builder().id(1L).build()));

        Specification<FileEntity> mockSpec = (root, query, cb) -> null;
        try (MockedStatic<FileSpecifications> specMock = mockStatic(FileSpecifications.class)) {
            specMock
                    .when(() -> FileSpecifications.withFilters("user", "name", List.of("tag")))
                    .thenReturn(mockSpec);

            when(repository.findAll(mockSpec, pageable)).thenReturn(page);

            // When
            Page<FileEntity> result =
                    fileService.searchDocuments("user", "name", List.of("tag"), pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        }
    }

    @Test
    void generateDownloadUrl_shouldReturnUrlIfExists() throws Exception {
        FileEntity file = FileEntity.builder().id(1L).minioPath("file-bucket/u/f.pdf").build();
        when(repository.findById(1L)).thenReturn(Optional.of(file));
        when(minioService.generateDownloadUrl("file-bucket/u/f.pdf"))
                .thenReturn("http://localhost:9000/file");

        Optional<String> result = fileService.generateDownloadUrl(1L);

        assertThat(result).isPresent().contains("http://localhost:9000/file");
    }

    @Test
    void generateDownloadUrl_shouldReturnEmptyIfNotFound() throws Exception {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        Optional<String> result = fileService.generateDownloadUrl(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void generateDownloadUrl_shouldReturnNullOnMinioFailure() throws Exception {
        FileEntity file = FileEntity.builder().id(1L).minioPath("bad-path").build();
        when(repository.findById(1L)).thenReturn(Optional.of(file));
        when(minioService.generateDownloadUrl("bad-path")).thenThrow(new RuntimeException());

        Optional<String> result = fileService.generateDownloadUrl(1L);

        assertThat(result).isEmpty();
        assertThat(result).isEmpty();
    }


}
