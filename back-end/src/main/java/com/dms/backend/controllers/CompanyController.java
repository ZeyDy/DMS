package com.dms.backend.controllers;

import com.dms.backend.models.Company;
import com.dms.backend.services.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<Company> registerCompany(@RequestBody Company company) {
        Company savedCompany = companyService.registerCompany(company);
        return ResponseEntity.ok(savedCompany);
    }
}
