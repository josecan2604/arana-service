package com.clara.ops.challenge.document_management_service_challenge;

import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.clara.ops.challenge.document_management_service_challenge.controller.FileController;
import com.clara.ops.challenge.document_management_service_challenge.entities.FileEntity;
import com.clara.ops.challenge.document_management_service_challenge.entities.TagEntity;
import com.clara.ops.challenge.document_management_service_challenge.service.FileService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(FileController.class)
@Import(FileControllerTestConfig.class)
public class FileControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private FileService fileService;

  private FileEntity fileEntity;

  @BeforeEach
  void setUp() {
    fileEntity =
        FileEntity.builder()
            .id(1L)
            .userName("testuser")
            .fileName("file.pdf")
            .fileType("application/pdf")
            .fileSize(1234L)
            .minioPath("testuser/file.pdf")
            .createdAt(Instant.now())
            .tags(List.of(new TagEntity(1L, "tag1", null))) // <- make sure this is not null
            .build();
  }

  @Test
  void testUploadFileSuccess() throws Exception {
    MockMultipartFile mockFile =
        new MockMultipartFile("file", "file.pdf", "application/pdf", "Fake content".getBytes());

    when(fileService.upload(anyString(), anyString(), anyList(), any()))
        .thenReturn(CompletableFuture.completedFuture(fileEntity));

    MvcResult mvcResult =
        mockMvc
            .perform(
                MockMvcRequestBuilders.multipart("/api/documents")
                    .file(mockFile)
                    .param("user", "testuser")
                    .param("fileName", "file.pdf")
                    .param("tags", "tag1", "tag2"))
            .andExpect(request().asyncStarted())
            .andReturn();

    mockMvc
        .perform(asyncDispatch(mvcResult))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.response.userName").value("testuser"));
  }

  @Test
  void testSearchDocuments() throws Exception {
    Page<FileEntity> page = new PageImpl<>(List.of(fileEntity));
    when(fileService.searchDocuments(any(), any(), anyList(), any())).thenReturn(page);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/documents")
                .param("user", "testuser")
                .param("fileName", "file.pdf")
                .param("tags", "tag1")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.response.content[0].fileName").value("file.pdf"));
  }

  @Test
  void testDownloadUrlFound() throws Exception {
    when(fileService.generateDownloadUrl(1L)).thenReturn(Optional.of("http://minio/download/url"));

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/documents/1/download"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.response.url").value("http://minio/download/url"));
  }

  @Test
  void testDownloadUrlNotFound() throws Exception {
    when(fileService.generateDownloadUrl(99L)).thenReturn(Optional.empty());

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/documents/99/download"))
        .andExpect(status().isNotFound());
  }

  @Test
  void testUploadFileMissingFileName() throws Exception {
    MockMultipartFile mockFile =
        new MockMultipartFile("file", "file.pdf", "application/pdf", "Fake content".getBytes());

    mockMvc
        .perform(
            MockMvcRequestBuilders.multipart("/api/documents")
                .file(mockFile)
                .param("user", "testuser")
                // Missing fileName param
                .param("tags", "tag1", "tag2"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  void testConcurrentUploads() throws Exception {
    int threadCount = 10;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    when(fileService.upload(anyString(), anyString(), anyList(), any()))
        .thenAnswer(
            invocation -> {
              // Simulate upload processing delay
              Thread.sleep(200);
              return CompletableFuture.completedFuture(fileEntity);
            });

    for (int i = 0; i < threadCount; i++) {
      int index = i;
      executor.submit(
          () -> {
            try {
              MockMultipartFile file =
                  new MockMultipartFile(
                      "file",
                      "file" + index + ".pdf",
                      "application/pdf",
                      ("Fake content " + index).getBytes());

              MvcResult result =
                  mockMvc
                      .perform(
                          MockMvcRequestBuilders.multipart("/api/documents")
                              .file(file)
                              .param("user", "user" + index)
                              .param("fileName", "file" + index + ".pdf")
                              .param("tags", "tag1", "tag2"))
                      .andExpect(request().asyncStarted())
                      .andReturn();

              mockMvc.perform(asyncDispatch(result)).andExpect(status().isCreated());

            } catch (Exception e) {
              fail("Upload failed in thread " + index + ": " + e.getMessage());
            } finally {
              latch.countDown();
            }
          });
    }

    latch.await(); // Wait for all threads to finish
    executor.shutdown();
  }
}
