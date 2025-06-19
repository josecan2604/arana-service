package com.clara.ops.challenge.document_management_service_challenge.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String user;
    private String documentName;
    private String fileType;
    private Long fileSize;
    private String minioPath;
    private Instant createdAt;

    @ElementCollection
    private List<String> tags;
}
