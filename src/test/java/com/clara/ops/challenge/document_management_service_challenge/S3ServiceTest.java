package com.clara.ops.challenge.document_management_service_challenge;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.clara.ops.challenge.document_management_service_challenge.service.MinioService;
import io.minio.*;
import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    MinioClient mockClient;

    @Mock
    MultipartFile multipartFile;

    MinioService service;

    @BeforeEach
    void setup() {
        service = new MinioService("http://localhost:9000", "root", "rootpass");
        ReflectionTestUtils.setField(service, "minioClient", mockClient);
        ReflectionTestUtils.setField(service, "bucketName", "test-bucket");
    }

    @Test
    void init_shouldCreateBucketIfNotExists() throws Exception {
        when(mockClient.bucketExists(any())).thenReturn(false);

        service.init();

        verify(mockClient).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void init_shouldNotCreateBucketIfExists() throws Exception {
        when(mockClient.bucketExists(any())).thenReturn(true);

        service.init();

        verify(mockClient, never()).makeBucket(any());
    }

    @Test
    void upload_shouldUploadFileSuccessfully() throws Exception {
        byte[] data = "sample content".getBytes();
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(data));
        when(multipartFile.getSize()).thenReturn((long) data.length);
        when(multipartFile.getContentType()).thenReturn("application/pdf");

        String result = service.upload("doc.pdf", multipartFile, "arana");

        assertThat(result).isEqualTo("arana/doc.pdf");

        verify(mockClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void upload_shouldThrowExceptionIfFails() throws Exception {
        when(multipartFile.getInputStream()).thenThrow(new RuntimeException("fail"));

        assertThatThrownBy(() -> service.upload("fail.pdf", multipartFile, "jane"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("fail");
    }

    @Test
    void generateDownloadUrl_shouldReturnPresignedUrl() throws Exception {
        String path = "arana/doc.pdf";
        String expectedUrl = "http://localhost:9000/test-bucket/arana/doc.pdf";

        when(mockClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn(expectedUrl);

        String result = service.generateDownloadUrl(path);

        assertThat(result).isEqualTo(expectedUrl);
    }

    @Test
    void generateDownloadUrl_shouldThrowIfMinioFails() throws Exception {
        when(mockClient.getPresignedObjectUrl(any())).thenThrow(new RuntimeException("minio error"));

        assertThatThrownBy(() -> service.generateDownloadUrl("x/y.pdf"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("minio error");
    }
}
