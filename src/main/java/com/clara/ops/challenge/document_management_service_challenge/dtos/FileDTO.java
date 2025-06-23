package com.clara.ops.challenge.document_management_service_challenge.dtos;

import java.time.Instant;
import java.util.Set;
import lombok.*;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FileDTO {
  private Long id;
  private String userName;

  public String getUserName() {
    return userName;
  }

  private String fileName;
  private String fileType;
  private Long fileSize;
  private String minioPath;
  private Instant createdAt;
  private Set<TagDTO> tag;
}
