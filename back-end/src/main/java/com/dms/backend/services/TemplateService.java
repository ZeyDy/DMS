package com.dms.backend.services;

import com.dms.backend.models.DocumentTemplate;
import com.dms.backend.repositories.DocumentTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final DocumentTemplateRepository templateRepository;

    @Value("${dms.storage.templates:./storage/templates}")
    private String templatesBasePath;

    public List<DocumentTemplate> getAllTemplates() {
        return templateRepository.findAll();
    }

    public DocumentTemplate uploadTemplate(MultipartFile file, String name, String description, String subfolder) throws IOException {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        String sanitizedSubfolder = subfolder.replaceAll("[^a-zA-Z0-9/_ -]", "_").trim();
        if (sanitizedSubfolder.isBlank()) {
            throw new RuntimeException("Subfolder is required");
        }

        Path targetDir = Paths.get(templatesBasePath, sanitizedSubfolder);
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        Path targetPath = targetDir.resolve(originalFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        DocumentTemplate template = DocumentTemplate.builder()
                .name(name)
                .fileName(originalFilename)
                .filePath(targetPath.toString())
                .description(description)
                .subfolder(sanitizedSubfolder)
                .build();

        return templateRepository.save(template);
    }

    public void deleteTemplate(Long id) throws IOException {
        DocumentTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        Path path = Paths.get(template.getFilePath());
        Files.deleteIfExists(path);

        templateRepository.delete(template);
    }
}
