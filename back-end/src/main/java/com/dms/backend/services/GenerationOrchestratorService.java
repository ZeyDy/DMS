package com.dms.backend.services;

import com.dms.backend.dto.GenerateDocumentsResponse;
import com.dms.backend.enums.ActionLogType;
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
    private final ActionLogService actionLogService;

    @Value("${dms.storage.templates:./storage/templates}")
    private String templatesBasePath;

    @Transactional
    public Long generateFullPackage(Long companyId, Long userId) throws IOException {
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

        actionLogService.logAction(userId, ActionLogType.DOCUMENT_GENERATION, generationRecord.getId(), "GENERATION_RECORD", "Full package generation for company: " + company.getName());

        return generationRecord.getId();
    }

    @Transactional
    public GenerateDocumentsResponse generateByAreas(Long companyId, List<String> selectedAreas, Long userId) throws IOException {
        if (selectedAreas == null || selectedAreas.isEmpty()) {
            throw new RuntimeException("At least one area must be selected");
        }

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
            throw new RuntimeException("Templates directory not found: " + templatesBasePath);
        }

        List<String> normalizedAreas = selectedAreas.stream()
                .map(a -> a.replace("/", FileSystems.getDefault().getSeparator())
                        .replace("\\", FileSystems.getDefault().getSeparator()))
                .toList();

        final int[] generatedCount = {0};

        try (Stream<Path> paths = Files.walk(templatesRootPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".docx"))
                    .filter(templatePath -> {
                        Path relativePath = templatesRootPath.relativize(templatePath);
                        String subfolder = relativePath.getParent() != null
                                ? relativePath.getParent().toString()
                                : "";
                        return normalizedAreas.stream().anyMatch(area ->
                                subfolder.equals(area) || subfolder.startsWith(area + FileSystems.getDefault().getSeparator())
                        );
                    })
                    .forEach(templatePath -> {
                        try {
                            generateForTemplateFromFileSystem(
                                    templatePath,
                                    templatesRootPath,
                                    companyRootPath,
                                    company,
                                    generationRecord
                            );
                            generatedCount[0]++;
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to generate document for template: " + templatePath, e);
                        }
                    });
        }

        if (generatedCount[0] == 0) {
            throw new RuntimeException("No template files found for selected areas: " + selectedAreas);
        }

        actionLogService.logAction(userId, ActionLogType.DOCUMENT_GENERATION, generationRecord.getId(), "GENERATION_RECORD", "Area-based generation for company: " + company.getName() + " (Areas: " + selectedAreas + ")");

        return GenerateDocumentsResponse.builder()
                .generationRecordId(generationRecord.getId())
                .documentCount(generatedCount[0])
                .message("Documents generated successfully")
                .build();
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
