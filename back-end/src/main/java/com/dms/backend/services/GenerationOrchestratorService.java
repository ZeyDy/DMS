package com.dms.backend.services;

import com.dms.backend.models.Company;
import com.dms.backend.models.DocumentTemplate;
import com.dms.backend.models.GeneratedDocument;
import com.dms.backend.repositories.CompanyRepository;
import com.dms.backend.repositories.DocumentTemplateRepository;
import com.dms.backend.repositories.GeneratedDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class GenerationOrchestratorService {

    private final CompanyRepository companyRepository;
    private final DocumentTemplateRepository templateRepository;
    private final GeneratedDocumentRepository generatedDocumentRepository;
    private final FolderStructureService folderStructureService;
    private final DocumentGeneratorService documentGeneratorService;

    @Value("${dms.storage.templates:./storage/templates}")
    private String templatesBasePath;

    @Transactional
    public void generateFullPackage(Long companyId) throws IOException {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + companyId));

        Path companyRootPath = folderStructureService.createCompanyFolderStructure(company);
        Path templatesRootPath = Paths.get(templatesBasePath);

        if (!Files.exists(templatesRootPath)) {
            Files.createDirectories(templatesRootPath);
            return;
        }

        try (Stream<Path> paths = Files.walk(templatesRootPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".docx"))
                    .forEach(templatePath -> {
                        try {
                            generateForTemplate(templatePath, templatesRootPath, companyRootPath, company);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to generate document for template: " + templatePath, e);
                        }
                    });
        }
    }

    private void generateForTemplate(Path templatePath, Path templatesRootPath, Path companyRootPath, Company company) throws IOException {
        Path relativePath = templatesRootPath.relativize(templatePath);
        Path outputPath = companyRootPath.resolve(relativePath);

        // Ensure subdirectories exist in target
        Files.createDirectories(outputPath.getParent());

        documentGeneratorService.generateDocument(templatePath, outputPath, company);

        // Log to database
        GeneratedDocument generatedDocument = GeneratedDocument.builder()
                .company(company)
                .fileName(outputPath.getFileName().toString())
                .filePath(outputPath.toString())
                .generationDate(LocalDateTime.now())
                .build();
        
        // Note: For now we don't strictly require DocumentTemplate to exist in DB for bulk generation 
        // as we are scanning the file system. In a more mature version, we'd sync them.
        generatedDocumentRepository.save(generatedDocument);
    }
}
