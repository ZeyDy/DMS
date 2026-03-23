package com.dms.backend.services;

import com.dms.backend.models.GenerationRecord;
import com.dms.backend.models.GeneratedDocument;
import com.dms.backend.repositories.GenerationRecordRepository;
import com.dms.backend.repositories.GeneratedDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class DocumentDownloadService {

    private final GenerationRecordRepository generationRecordRepository;
    private final GeneratedDocumentRepository generatedDocumentRepository;

    public byte[] zipGenerationRecordDocuments(Long generationRecordId) throws IOException {
        GenerationRecord generationRecord = generationRecordRepository.findById(generationRecordId)
                .orElseThrow(() -> new RuntimeException("Generation record not found with id: " + generationRecordId));

        List<GeneratedDocument> documents = generatedDocumentRepository.findAllByGenerationRecordId(generationRecordId);

        if (documents.isEmpty()) {
            throw new RuntimeException("No generated documents found for generation record id: " + generationRecordId);
        }

        String rootFolderName = sanitize(generationRecord.getCompany().getName()) + "_" + generationRecord.getCompany().getCode();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            for (GeneratedDocument document : documents) {
                Path filePath = Paths.get(document.getFilePath());

                if (!Files.exists(filePath)) {
                    continue;
                }

                String entryName;
                if (document.getTemplate() != null && document.getTemplate().getSubfolder() != null) {
                    entryName = rootFolderName + "/" +
                            document.getTemplate().getSubfolder() + "/" +
                            document.getFileName();
                } else {
                    entryName = rootFolderName + "/" + document.getFileName();
                }

                try {
                    zipOutputStream.putNextEntry(new ZipEntry(entryName.replace("\\", "/")));
                    Files.copy(filePath, zipOutputStream);
                    zipOutputStream.closeEntry();
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed to add file to ZIP: " + filePath, e);
                }
            }
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }

        return outputStream.toByteArray();
    }

    public String buildZipFileName(Long generationRecordId) {
        GenerationRecord generationRecord = generationRecordRepository.findById(generationRecordId)
                .orElseThrow(() -> new RuntimeException("Generation record not found with id: " + generationRecordId));

        return sanitize(generationRecord.getCompany().getName()) + "_" +
                generationRecord.getCompany().getCode() +
                "_generation_" + generationRecordId + ".zip";
    }

    private String sanitize(String value) {
        return value.replaceAll("[\\\\/:*?\"<>|]", "_").replaceAll("\\s+", "_");
    }
}
