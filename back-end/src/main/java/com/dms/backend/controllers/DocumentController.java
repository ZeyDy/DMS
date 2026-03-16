package com.dms.backend.controllers;

import com.dms.backend.services.GenerationOrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final GenerationOrchestratorService generationOrchestratorService;

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
}
