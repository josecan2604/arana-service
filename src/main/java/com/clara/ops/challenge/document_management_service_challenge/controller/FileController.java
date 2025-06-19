package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.entities.FileEntity;
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
import java.util.concurrent.CompletableFuture;

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
            @ApiResponse(responseCode = "201", description = "File Uploaded"),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<?> upload(@RequestParam String user,
                                           @RequestParam String documentName,
                                           @RequestParam List<String> tags,
                                           @RequestParam MultipartFile file) throws Exception {
        if (user == null || user.trim().isEmpty()) {
            return new ResponseEntity<>("User must not be blank.", HttpStatus.BAD_REQUEST);
        }

        if (documentName == null || documentName.trim().isEmpty()) {
            return new ResponseEntity<>("Document name must not be blank.", HttpStatus.BAD_REQUEST);
        }

        if (tags == null || tags.isEmpty()) {
            return new ResponseEntity<>("At least one tag is required.", HttpStatus.BAD_REQUEST);
        }

        if (file == null || file.isEmpty() || !"application/pdf".equalsIgnoreCase(file.getContentType())) {
            return new ResponseEntity<>("File must be a non-empty PDF.", HttpStatus.BAD_REQUEST);
        }

        CompletableFuture<FileEntity> completableFuture = service.upload(user, documentName, tags, file);

        return new ResponseEntity<>( completableFuture.get(), HttpStatus.CREATED);
        }

        @GetMapping
        @Operation(summary = "Search documents by user, name, tags with pagination and filter")
        public ResponseEntity<Page<FileEntity>> search(@RequestParam(required = false) String user,
                                                       @RequestParam(required = false) String documentName,
                                                       @RequestParam(required = false) List<String> tags,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int size) {

            PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<FileEntity> results = service.search(user, documentName, tags,  pageable);

            return new ResponseEntity<>(results, HttpStatus.CREATED);
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
    }
