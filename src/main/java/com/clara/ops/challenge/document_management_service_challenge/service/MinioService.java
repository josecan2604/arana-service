package com.clara.ops.challenge.document_management_service_challenge.service;

import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MinioService {
  private final MinioClient minioClient;

  @Value("${minio.bucket.name}")
  private String bucketName;

  public MinioService(
      @Value("${minio.url}") String url,
      @Value("${minio.access.key}") String accessKey,
      @Value("${minio.secret.key}") String secretKey) {
    this.minioClient =
        MinioClient.builder().endpoint(url).credentials(accessKey, secretKey).build();
  }

  @PostConstruct
  public void init() throws Exception {
    boolean exists =
        minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    if (!exists) {
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    }
  }

  public String upload(String objectName, MultipartFile file, String user) throws Exception {
    String objectPath = user + "/" + objectName;
    try (InputStream in = file.getInputStream()) {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucketName).object(objectPath).stream(
                  in, file.getSize(), -1)
              .contentType(file.getContentType())
              .build());
    }
    return objectPath;
  }

  public String generateDownloadUrl(String objectPath) throws Exception {
    return minioClient.getPresignedObjectUrl(
        GetPresignedObjectUrlArgs.builder()
            .method(Method.GET)
            .bucket(bucketName)
            .object(objectPath)
            .expiry(10, TimeUnit.MINUTES)
            .build());









  }
}
