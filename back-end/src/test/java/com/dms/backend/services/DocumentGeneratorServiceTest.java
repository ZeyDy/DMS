package com.dms.backend.services;

import com.dms.backend.enums.CompanyType;
import com.dms.backend.enums.ManagerType;
import com.dms.backend.models.Company;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentGeneratorServiceTest {

    private final DocumentGeneratorService documentGeneratorService = new DocumentGeneratorService();

    @TempDir
    Path tempDir;

    @Test
    void testGenerateDocument_ReplacesPlaceholders() throws IOException {
        // Prepare template
        Path templatePath = tempDir.resolve("template.docx");
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph p1 = doc.createParagraph();
            p1.createRun().setText("Company Name: ${name}");
            XWPFParagraph p2 = doc.createParagraph();
            p2.createRun().setText("Code: ${code}, Address: ${address}");
            
            try (FileOutputStream out = new FileOutputStream(templatePath.toFile())) {
                doc.write(out);
            }
        }

        // Prepare data
        Company company = Company.builder()
                .name("Test Company")
                .code("TC001")
                .address("123 Test St")
                .type(CompanyType.UAB)
                .managerType(ManagerType.DIRECTOR)
                .build();

        Path outputPath = tempDir.resolve("output.docx");

        // Execute
        documentGeneratorService.generateDocument(templatePath, outputPath, company);

        // Verify
        assertTrue(Files.exists(outputPath));
        try (XWPFDocument resultDoc = new XWPFDocument(Files.newInputStream(outputPath))) {
            String fullText = getFullText(resultDoc);
            assertTrue(fullText.contains("Company Name: Test Company"));
            assertTrue(fullText.contains("Code: TC001, Address: 123 Test St"));
        }
    }

    private String getFullText(XWPFDocument doc) {
        StringBuilder sb = new StringBuilder();
        for (XWPFParagraph p : doc.getParagraphs()) {
            sb.append(p.getText()).append("\n");
        }
        return sb.toString();
    }
}
