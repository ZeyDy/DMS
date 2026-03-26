package com.dms.backend.controllers;

import com.dms.backend.dto.GenerateByAreasRequest;
import com.dms.backend.models.GeneratedDocument;
import com.dms.backend.models.GenerationRecord;
import com.dms.backend.repositories.GeneratedDocumentRepository;
import com.dms.backend.repositories.GenerationRecordRepository;
import com.dms.backend.services.DocumentDownloadService;
import com.dms.backend.services.GenerationOrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final GenerationOrchestratorService generationOrchestratorService;
    private final DocumentDownloadService documentDownloadService;
    private final GenerationRecordRepository generationRecordRepository;
    private final GeneratedDocumentRepository generatedDocumentRepository;

    @PostMapping("/generate-package/company/{id}")
    public ResponseEntity<?> generatePackage(@PathVariable Long id) {
        try {
            Long generationRecordId = generationOrchestratorService.generateFullPackage(id);
            return ResponseEntity.ok(Map.of("generationRecordId", generationRecordId));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error generating document package: " + e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/generate-by-areas")
    public ResponseEntity<?> generateByAreas(@RequestBody GenerateByAreasRequest request) {
        try {
            var response = generationOrchestratorService.generateByAreas(request.getCompanyId(), request.getSelectedAreas());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error generating selected documents: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getGenerationHistory() {
        List<GenerationRecord> records = generationRecordRepository.findAllByOrderByCreatedAtDesc();

        List<Map<String, Object>> result = records.stream().map(record -> {
            List<GeneratedDocument> docs = generatedDocumentRepository.findAllByGenerationRecordId(record.getId());

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", record.getId());
            item.put("companyName", record.getCompany().getType() + " " + record.getCompany().getName());
            item.put("companyCode", record.getCompany().getCode());
            item.put("createdAt", record.getCreatedAt());
            item.put("documentCount", docs.size());
            return item;
        }).toList();

        return ResponseEntity.ok(result);
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
