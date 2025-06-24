package com.clara.ops.challenge.document_management_service_challenge.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "tags",
    indexes = {@Index(name = "idx_tags_name", columnList = "name")})
public class TagEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @ManyToMany(mappedBy = "tags")
  private List<FileEntity> files = new ArrayList<>();

  public TagEntity(String name) {
    this.name = name;
  }
}
