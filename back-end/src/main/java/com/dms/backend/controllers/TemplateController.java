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
    public List<DocumentTemplate> listTemplates(@RequestParam(value = "userId", required = false) Long userId) {
        return templateService.getAllTemplates(userId);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadTemplate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "subfolder", defaultValue = "") String subfolder,
            @RequestParam("userId") Long userId) {
        try {
            DocumentTemplate template = templateService.uploadTemplate(file, name, description, subfolder, userId);
            return ResponseEntity.ok(template);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to upload template: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/sharing")
    public ResponseEntity<?> toggleSharing(
            @PathVariable Long id,
            @RequestParam("userId") Long userId,
            @RequestParam("isShared") boolean isShared) {
        try {
            templateService.toggleSharing(id, userId, isShared);
            return ResponseEntity.ok("Sharing settings updated");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTemplate(
            @PathVariable Long id,
            @RequestParam("userId") Long userId) {
        try {
            templateService.deleteTemplate(id, userId);
            return ResponseEntity.ok("Template deleted successfully");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to delete template: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
