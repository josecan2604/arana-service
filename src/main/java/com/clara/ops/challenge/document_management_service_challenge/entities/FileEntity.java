package com.clara.ops.challenge.document_management_service_challenge.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "file_entity", indexes = {
        @Index(name = "idx_file_user_name", columnList = "user_name"),
        @Index(name = "idx_file_file_name", columnList = "file_name"),
        @Index(name = "idx_file_created_at", columnList = "created_at")
})
public class FileEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String userName;
  private String fileName;
  private String fileType;
  private Long fileSize;
  private String minioPath;

  @Column(name = "created_at")
  private Instant createdAt;

  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinTable(
      name = "file_tags",
      joinColumns = @JoinColumn(name = "file_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  @JsonIgnore
  private List<TagEntity> tags = new ArrayList<>();
}
