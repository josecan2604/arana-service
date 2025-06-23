package com.clara.ops.challenge.document_management_service_challenge;

import com.clara.ops.challenge.document_management_service_challenge.service.FileService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileControllerTestConfig {
  @Bean
  public FileService fileService() {
    return Mockito.mock(FileService.class);
  }
}
