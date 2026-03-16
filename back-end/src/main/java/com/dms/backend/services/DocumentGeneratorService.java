package com.dms.backend.services;

import com.dms.backend.models.Company;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocumentGeneratorService {

    public void generateDocument(Path templatePath, Path outputPath, Company company) throws IOException {
        try (FileInputStream fis = new FileInputStream(templatePath.toFile());
             XWPFDocument document = new XWPFDocument(fis)) {

            Map<String, String> replacements = createReplacementMap(company);

            // Replace in paragraphs
            replaceInParagraphs(document.getParagraphs(), replacements);

            // Replace in tables
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        replaceInParagraphs(cell.getParagraphs(), replacements);
                    }
                }
            }

            try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                document.write(fos);
            }
        }
    }

    private void replaceInParagraphs(List<XWPFParagraph> paragraphs, Map<String, String> replacements) {
        for (XWPFParagraph paragraph : paragraphs) {
            List<XWPFRun> runs = paragraph.getRuns();
            if (runs != null) {
                for (XWPFRun run : runs) {
                    String text = run.getText(0);
                    if (text != null) {
                        for (Map.Entry<String, String> entry : replacements.entrySet()) {
                            if (text.contains(entry.getKey())) {
                                text = text.replace(entry.getKey(), entry.getValue() != null ? entry.getValue() : "");
                                run.setText(text, 0);
                            }
                        }
                    }
                }
            }
        }
    }

    private Map<String, String> createReplacementMap(Company company) {
        Map<String, String> map = new HashMap<>();
        map.put("${type}", company.getType() != null ? company.getType().toString() : "");
        map.put("${name}", company.getName());
        map.put("${code}", company.getCode());
        map.put("${category}", company.getCategory());
        map.put("${address}", company.getAddress());
        map.put("${cityOrDistrict}", company.getCityOrDistrict());
        map.put("${managerType}", company.getManagerType() != null ? company.getManagerType().toString() : "");
        map.put("${managerFullName}", company.getManagerFullName());
        map.put("${documentDate}", company.getDocumentDate());
        return map;
    }
}
