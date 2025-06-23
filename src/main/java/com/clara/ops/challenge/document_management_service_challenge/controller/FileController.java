package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.dtos.ApiResponseWrapper;
import com.clara.ops.challenge.document_management_service_challenge.dtos.FileDTO;
import com.clara.ops.challenge.document_management_service_challenge.dtos.TagDTO;
import com.clara.ops.challenge.document_management_service_challenge.entities.FileEntity;
import com.clara.ops.challenge.document_management_service_challenge.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "201",
        description = "File Uploaded"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "Bad Request")
  })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseBody
  public CompletableFuture<ResponseEntity<ApiResponseWrapper<FileDTO>>> upload(
      @RequestParam String user,
      @RequestParam String fileName,
      @RequestParam List<String> tags,
      @RequestParam MultipartFile file)
      throws Exception {

    if (user == null || user.trim().isEmpty()) {
      return CompletableFuture.completedFuture(
          ResponseEntity.badRequest()
              .body(new ApiResponseWrapper<>(null, "User must not be blank.", 400)));
    }

    if (fileName == null || fileName.trim().isEmpty()) {
      return CompletableFuture.completedFuture(
          ResponseEntity.badRequest()
              .body(new ApiResponseWrapper<>(null, "fileName must not be blank.", 400)));
    }

    if (tags == null || tags.isEmpty()) {
      return CompletableFuture.completedFuture(
          ResponseEntity.badRequest()
              .body(new ApiResponseWrapper<>(null, "tags must not be blank.", 400)));
    }

    if (file == null
        || file.isEmpty()
        || !"application/pdf".equalsIgnoreCase(file.getContentType())) {
      return CompletableFuture.completedFuture(
          ResponseEntity.badRequest()
              .body(
                  new ApiResponseWrapper<>(null, "file must not be blank and in Pdf format", 400)));
    }

    return service
        .upload(user, fileName, tags, file)
        .thenApply(
            l -> {
              FileDTO fileTransfer = mapToDto(l);
              ApiResponseWrapper<FileDTO> wrapper =
                  new ApiResponseWrapper<>(fileTransfer, "Success", 201);
              return ResponseEntity.status(HttpStatus.CREATED).body(wrapper);
            })
        .exceptionally(
            ex -> {
              int status = (ex.getCause() instanceof IllegalStateException) ? 400 : 500;
              String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();

              ApiResponseWrapper<FileDTO> wrapper =
                  new ApiResponseWrapper<>(null, message, status);
              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(wrapper);
            });
  }

  @GetMapping
  @Operation(summary = "Search documents by user, name, tags with pagination and filter")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "File Uploaded"),
  })
  public ResponseEntity<ApiResponseWrapper<Page<FileDTO>>> search(
      @RequestParam(required = false) String user,
      @RequestParam(required = false) String fileName,
      @RequestParam(required = false) List<String> tags,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<FileEntity> results = service.searchDocuments(user, fileName, tags, pageable);
    Page<FileDTO> file = results.map(this::mapToDto);

    return ResponseEntity.ok(new ApiResponseWrapper<>(file, "Success", 200));
  }

  @GetMapping("/{id}/download")
  @Operation(summary = "Generate pre-signed download URL by ID")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "Not found")
  })
  public ResponseEntity<ApiResponseWrapper<Map<String, String>>> download(@PathVariable Long id)
      throws Exception {
    return service
        .generateDownloadUrl(id)
        .map(
            url ->
                ResponseEntity.ok(new ApiResponseWrapper<>((Map.of("url", url)), "Success", 200)))
        .orElse(
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponseWrapper<>(null, "Not found", 400)));
  }

  private FileDTO mapToDto(FileEntity file) {
    Set<TagDTO> tagDtos =
        file.getTags().stream()
            .map(tag -> new TagDTO(tag.getId(), tag.getName()))
            .collect(Collectors.toSet());

    return new FileDTO(
        file.getId(),
        file.getUserName(),
        file.getFileName(),
        file.getFileType(),
        file.getFileSize(),
        file.getMinioPath(),
        file.getCreatedAt(),
        tagDtos);
  }
}
