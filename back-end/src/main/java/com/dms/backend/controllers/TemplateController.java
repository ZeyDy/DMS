package com.dms.backend.controllers;

import com.dms.backend.models.DocumentTemplate;
import com.dms.backend.services.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    public List<DocumentTemplate> listTemplates() {
        return templateService.getAllTemplates();
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadTemplate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "subfolder", defaultValue = "") String subfolder) {
        try {
            DocumentTemplate template = templateService.uploadTemplate(file, name, description, subfolder);
            return ResponseEntity.ok(template);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to upload template: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTemplate(@PathVariable Long id) {
        try {
            templateService.deleteTemplate(id);
            return ResponseEntity.ok("Template deleted successfully");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to delete template: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
