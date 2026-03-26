package com.dms.backend.services;

import com.dms.backend.dto.GenerateDocumentsResponse;
import com.dms.backend.models.Company;
import com.dms.backend.models.DocumentTemplate;
import com.dms.backend.models.GeneratedDocument;
import com.dms.backend.models.GenerationRecord;
import com.dms.backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class GenerationOrchestratorService {

    private final CompanyRepository companyRepository;
    private final DocumentTemplateRepository templateRepository;
    private final GeneratedDocumentRepository generatedDocumentRepository;
    private final GenerationRecordRepository generationRecordRepository;
    private final FolderStructureService folderStructureService;
    private final DocumentGeneratorService documentGeneratorService;

    @Value("${dms.storage.templates:./storage/templates}")
    private String templatesBasePath;

    @Transactional
    public Long generateFullPackage(Long companyId) throws IOException {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + companyId));

        GenerationRecord generationRecord = generationRecordRepository.save(
                GenerationRecord.builder()
                        .company(company)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        Path companyRootPath = folderStructureService.createCompanyFolderStructure(company);
        Path templatesRootPath = Paths.get(templatesBasePath);

        if (!Files.exists(templatesRootPath)) {
            Files.createDirectories(templatesRootPath);
            return generationRecord.getId();
        }

        try (Stream<Path> paths = Files.walk(templatesRootPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".docx"))
                    .forEach(templatePath -> {
                        try {
                            generateForTemplateFromFileSystem(
                                    templatePath,
                                    templatesRootPath,
                                    companyRootPath,
                                    company,
                                    generationRecord
                            );
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to generate document for template: " + templatePath, e);
                        }
                    });
        }

        return generationRecord.getId();
    }

    @Transactional
    public GenerateDocumentsResponse generateByAreas(Long companyId, List<String> selectedAreas) throws IOException {
        if (selectedAreas == null || selectedAreas.isEmpty()) {
            throw new RuntimeException("At least one area must be selected");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + companyId));

        List<DocumentTemplate> templates = templateRepository.findAllBySubfolderIn(selectedAreas);

        if (templates.isEmpty()) {
            throw new RuntimeException("No templates found for selected areas");
        }

        GenerationRecord generationRecord = generationRecordRepository.save(
                GenerationRecord.builder()
                        .company(company)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        Path companyRootPath = folderStructureService.createCompanyFolderStructure(company);

        int generatedCount = 0;

        for (DocumentTemplate template : templates) {
            generateForTemplateFromDatabase(template, companyRootPath, company, generationRecord);
            generatedCount++;
        }

        return GenerateDocumentsResponse.builder()
                .generationRecordId(generationRecord.getId())
                .documentCount(generatedCount)
                .message("Documents generated successfully")
                .build();
    }

    private void generateForTemplateFromDatabase(
            DocumentTemplate template,
            Path companyRootPath,
            Company company,
            GenerationRecord generationRecord
    ) throws IOException {

        Path templatePath = Paths.get(template.getFilePath());

        if (!Files.exists(templatePath)) {
            throw new RuntimeException("Template file not found: " + template.getFilePath());
        }

        Path outputDir = companyRootPath.resolve(template.getSubfolder());
        Files.createDirectories(outputDir);

        Path outputPath = outputDir.resolve(template.getFileName());

        documentGeneratorService.generateDocument(templatePath, outputPath, company);

        GeneratedDocument generatedDocument = GeneratedDocument.builder()
                .company(company)
                .template(template)
                .generationRecord(generationRecord)
                .fileName(outputPath.getFileName().toString())
                .filePath(outputPath.toString())
                .generationDate(LocalDateTime.now())
                .build();

        generatedDocumentRepository.save(generatedDocument);
    }

    private void generateForTemplateFromFileSystem(
            Path templatePath,
            Path templatesRootPath,
            Path companyRootPath,
            Company company,
            GenerationRecord generationRecord
    ) throws IOException {

        Path relativePath = templatesRootPath.relativize(templatePath);
        Path outputPath = companyRootPath.resolve(relativePath);

        Files.createDirectories(outputPath.getParent());

        documentGeneratorService.generateDocument(templatePath, outputPath, company);

        String templateFilePath = templatePath.toString();
        String fileName = templatePath.getFileName().toString();
        String subfolder = relativePath.getParent() != null
                ? relativePath.getParent().toString()
                : "";
        String templateName = fileName.replace(".docx", "");

        DocumentTemplate template = templateRepository.findByFilePath(templateFilePath)
                .orElseGet(() -> templateRepository.save(
                        DocumentTemplate.builder()
                                .name(templateName)
                                .fileName(fileName)
                                .filePath(templateFilePath)
                                .subfolder(subfolder)
                                .build()
                ));

        GeneratedDocument generatedDocument = GeneratedDocument.builder()
                .company(company)
                .template(template)
                .generationRecord(generationRecord)
                .fileName(outputPath.getFileName().toString())
                .filePath(outputPath.toString())
                .generationDate(LocalDateTime.now())
                .build();

        generatedDocumentRepository.save(generatedDocument);
    }
}