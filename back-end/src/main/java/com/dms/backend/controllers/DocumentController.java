package com.dms.backend.controllers;

import com.dms.backend.dto.GenerateByAreasRequest;
import com.dms.backend.services.DocumentDownloadService;
import com.dms.backend.services.GenerationOrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final GenerationOrchestratorService generationOrchestratorService;
    private final DocumentDownloadService documentDownloadService;

    @PostMapping("/generate-package/company/{id}")
    public ResponseEntity<String> generatePackage(@PathVariable Long id) {
        try {
            generationOrchestratorService.generateFullPackage(id);
            return ResponseEntity.ok("Document package generation started/completed successfully for company ID: " + id);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error generating document package: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/generate-by-areas")
    public ResponseEntity<?> generateByAreas(@RequestBody GenerateByAreasRequest request) {
        try {
            generationOrchestratorService.generateByAreas(request.getCompanyId(), request.getSelectedAreas());
            return ResponseEntity.ok("Documents generated successfully for selected areas");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error generating selected documents: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/download/{generationRecordId}")
    public ResponseEntity<?> downloadGeneratedDocuments(@PathVariable Long generationRecordId) {
        try {
            byte[] zipBytes = documentDownloadService.zipGenerationRecordDocuments(generationRecordId);
            String fileName = documentDownloadService.buildZipFileName(generationRecordId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(zipBytes.length)
                    .body(zipBytes);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error creating ZIP file: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
