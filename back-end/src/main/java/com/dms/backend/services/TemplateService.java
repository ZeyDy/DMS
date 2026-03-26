package com.dms.backend.services;

import com.dms.backend.enums.ActionLogType;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final DocumentTemplateRepository templateRepository;
    private final UserRepository userRepository;
    private final ActionLogService actionLogService;

    @Value("${dms.storage.templates:./storage/templates}")
    private String templatesBasePath;

    public List<DocumentTemplate> getAllTemplates(Long userId) {
        if (userId == null) return List.of();
        
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

        Path storagePath = Paths.get(templatesBasePath, subfolder);
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        Path filePath = storagePath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        DocumentTemplate template = DocumentTemplate.builder()
                .name(name)
                .description(description)
                .fileName(fileName)
                .filePath(filePath.toString())
                .subfolder(subfolder)
                .owner(owner)
                .isShared(false)
                .build();

        DocumentTemplate saved = templateRepository.save(template);
        actionLogService.logAction(ownerId, ActionLogType.TEMPLATE_UPLOAD, saved.getId(), "TEMPLATE", "Uploaded template: " + saved.getName());
        return saved;
    }

    @Transactional
    public void toggleSharing(Long templateId, Long userId, boolean isShared) {
        DocumentTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.ADMIN && !template.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Only owner or admin can change sharing settings");
        }

        template.setShared(isShared);
        templateRepository.save(template);
        actionLogService.logAction(userId, ActionLogType.TEMPLATE_SHARE_TOGGLE, template.getId(), "TEMPLATE", "Template '" + template.getName() + "' sharing set to " + isShared);
    }

    @Transactional
    public void deleteTemplate(Long templateId, Long userId) {
        DocumentTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.ADMIN && !template.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Only owner or admin can delete this template");
        }

        templateRepository.delete(template);
        actionLogService.logAction(userId, ActionLogType.TEMPLATE_DELETE, templateId, "TEMPLATE", "Deleted template: " + template.getName());
    }
}
