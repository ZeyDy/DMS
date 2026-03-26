package com.dms.backend.controllers;

import com.dms.backend.models.Company;
import com.dms.backend.services.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public Company registerCompany(@RequestBody Company company, @RequestParam(required = false) Long userId) {
        return companyService.registerCompany(company, userId);
    }

    @GetMapping
    public ResponseEntity<List<Company>> getAllCompanies() {
        List<Company> companies = companyService.getAllCompanies();
        return ResponseEntity.ok(companies);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Company>> searchCompanies(@RequestParam String name) {
        List<Company> companies = companyService.searchCompaniesByName(name);
        return ResponseEntity.ok(companies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable Long id) {
        Company company = companyService.getCompanyById(id);
        return ResponseEntity.ok(company);
    }

    @PutMapping("/{id}")
    public Company updateCompany(@PathVariable Long id, @RequestBody Company company, @RequestParam(required = false) Long userId) {
        return companyService.updateCompany(id, company, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteCompany(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        companyService.deleteCompany(id, userId);
    }
}
