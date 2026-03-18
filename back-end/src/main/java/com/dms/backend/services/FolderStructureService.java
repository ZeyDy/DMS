package com.dms.backend.services;

import com.dms.backend.models.Company;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Service
public class FolderStructureService {

    @Value("${dms.storage.generated:./storage/generated}")
    private String generatedBasePath;

    private static final List<String> SUBFOLDERS = Arrays.asList(
            "DSSI",
            "TVARKOS",
            "AAP",
            "PAREIGINIAI NUOSTATAI",
            "PRIEDAI",
            "MOKYMAI/GS",
            "MOKYMAI/Krovos rankomis"
    );

    public Path createCompanyFolderStructure(Company company) throws IOException {
        String companyFolderName = company.getName().replaceAll("[\\\\/:*?\"<>|]", "_") + "_" + company.getCode();
        Path companyPath = Paths.get(generatedBasePath, companyFolderName);

        if (!Files.exists(companyPath)) {
            Files.createDirectories(companyPath);
        }

        for (String subfolder : SUBFOLDERS) {
            Path subfolderPath = companyPath.resolve(subfolder);
            if (!Files.exists(subfolderPath)) {
                Files.createDirectories(subfolderPath);
            }
        }

        return companyPath;
    }
}
