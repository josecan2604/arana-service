package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.dtos.FileDTO;
import com.clara.ops.challenge.document_management_service_challenge.dtos.TagDTO;
import com.clara.ops.challenge.document_management_service_challenge.entities.FileEntity;
import com.clara.ops.challenge.document_management_service_challenge.entities.TagEntity;
import com.clara.ops.challenge.document_management_service_challenge.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Files", description = "Files management API")
public class FileController {
        private final FileService service;

        public FileController(FileService service) {

            this.service = service;
        }

    @Operation(summary = "Upload a PDF document")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File Uploaded"),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public CompletableFuture<ResponseEntity<?>> upload(@RequestParam String user,
                                           @RequestParam String fileName,
                                           @RequestParam List<String> tags,
                                           @RequestParam MultipartFile file) throws Exception {
        if (user == null || user.trim().isEmpty()) {
            return  CompletableFuture
                    .completedFuture(ResponseEntity
                            .badRequest()
                            .body("User must not be blank."));
        }

        if (fileName == null || fileName.trim().isEmpty()) {
            return  CompletableFuture
                    .completedFuture(ResponseEntity
                            .badRequest()
                            .body("Filenane must not be blank."));        }

        if (tags == null || tags.isEmpty()) {
            return  CompletableFuture
                    .completedFuture(ResponseEntity
                            .badRequest()
                            .body("At leas one tag is required "));        }

        if (file == null || file.isEmpty() || !"application/pdf".equalsIgnoreCase(file.getContentType())) {
            return  CompletableFuture
                    .completedFuture(ResponseEntity
                            .badRequest()
                            .body("A not blank Pdf file is required ."));
        }

        return service.upload(user, fileName, tags, file)
                .thenApply( l -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(l));

        }

        @GetMapping
        @Operation(summary = "Search documents by user, name, tags with pagination and filter")
        public ResponseEntity<Page<FileDTO>> search(@RequestParam(required = false) String user,
                                                       @RequestParam(required = false) String fileName,
                                                       @RequestParam(required = false) List<String> tags,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {

            PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<FileEntity> results = service.searchDocuments(user, fileName, tags, pageable);
            Page<FileDTO> dtoPage = results.map(this::mapToDto);

            return  ResponseEntity.ok(dtoPage);
        }

        @GetMapping("/{id}/download")
        @Operation(summary = "Generate pre-signed download URL by ID")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "OK"),
                @ApiResponse(responseCode = "400", description = "Not found")
        })
        public ResponseEntity<Map<String, String>> download(@PathVariable Long id) throws Exception {
            return service.generateDownloadUrl(id)
                    .map(url -> ResponseEntity.ok(Map.of("url", url)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }
    private FileDTO mapToDto(FileEntity file) {
        Set<TagDTO> tagDtos = file.getTags().stream()
                .map(tag -> new TagDTO (tag.getId(), tag.getName()))
                .collect(Collectors.toSet());

        return new FileDTO(
                file.getId(),
                file.getUserName(),
                file.getFileName(),
                file.getFileType(),
                file.getFileSize(),
                file.getMinioPath(),
                file.getCreatedAt(),
                tagDtos
        );
    }

}
