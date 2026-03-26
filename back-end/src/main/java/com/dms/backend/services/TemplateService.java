package com.dms.backend.services;

import com.dms.backend.enums.Role;
import com.dms.backend.models.DocumentTemplate;
import com.dms.backend.models.User;
import com.dms.backend.repositories.DocumentTemplateRepository;
import com.dms.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final UserRepository userRepository;

    @Value("${dms.storage.templates:./storage/templates}")
    private String templatesBasePath;

    public List<DocumentTemplate> getAllTemplates(Long userId) {
        if (userId == null) {
            return templateRepository.findAll(); // Or returned shared only? Let's say all for admin-like view if no user
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() == Role.ADMIN) {
            return templateRepository.findAll();
        }
        
        return templateRepository.findByOwnerOrIsSharedTrue(user);
    }

    @Transactional
    public DocumentTemplate uploadTemplate(MultipartFile file, String name, String description, String subfolder, Long ownerId) throws IOException {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
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
                .owner(owner)
                .isShared(false)
                .build();

        return templateRepository.save(template);
    }

    @Transactional
    public void toggleSharing(Long templateId, Long userId, boolean isShared) {
        DocumentTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!template.getOwner().equals(user) && user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only owner or admin can change sharing settings");
        }

        template.setShared(isShared);
        templateRepository.save(template);
    }

    @Transactional
    public void deleteTemplate(Long id, Long userId) throws IOException {
        DocumentTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!template.getOwner().equals(user) && user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only owner or admin can delete this template");
        }

        Path path = Paths.get(template.getFilePath());
        Files.deleteIfExists(path);

        templateRepository.delete(template);
    }
}
