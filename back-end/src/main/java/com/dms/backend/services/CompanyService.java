package com.dms.backend.services;

import com.dms.backend.enums.ActionLogType;
import com.dms.backend.models.Company;
import com.dms.backend.repositories.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final ActionLogService actionLogService;

    @Transactional
    public Company registerCompany(Company company, Long userId) {
        validateCompany(company);
        Company saved = companyRepository.save(company);
        actionLogService.logAction(userId, ActionLogType.COMPANY_CREATE, saved.getId(), "COMPANY", "Created company: " + saved.getName());
        return saved;
    }

    @Transactional
    public Company updateCompany(Long id, Company updatedCompany, Long userId) {
        Company existingCompany = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + id));

        validateUpdatedCompany(id, updatedCompany);

        existingCompany.setType(updatedCompany.getType());
        existingCompany.setName(updatedCompany.getName());
        // ... (remaining fields)
        existingCompany.setCode(updatedCompany.getCode());
        existingCompany.setCategory(updatedCompany.getCategory());
        existingCompany.setAddress(updatedCompany.getAddress());
        existingCompany.setCityOrDistrict(updatedCompany.getCityOrDistrict());
        existingCompany.setManagerType(updatedCompany.getManagerType());
        existingCompany.setManagerFullName(updatedCompany.getManagerFullName());
        existingCompany.setDocumentDate(updatedCompany.getDocumentDate());

        Company saved = companyRepository.save(existingCompany);
        actionLogService.logAction(userId, ActionLogType.COMPANY_UPDATE, saved.getId(), "COMPANY", "Updated company: " + saved.getName());
        return saved;
    }

    @Transactional
    public void deleteCompany(Long id, Long userId) {
        Company existingCompany = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + id));

        companyRepository.delete(existingCompany);
        actionLogService.logAction(userId, ActionLogType.COMPANY_DELETE, id, "COMPANY", "Deleted company: " + existingCompany.getName());
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + id));
    }

    public List<Company> searchCompaniesByName(String name) {
        if (!StringUtils.hasText(name)) {
            return List.of();
        }
        return companyRepository.findByNameContainingIgnoreCase(name);
    }

    private void validateCompany(Company company) {
        if (company == null) {
            throw new IllegalArgumentException("Company cannot be null");
        }
        if (!StringUtils.hasText(company.getName())) {
            throw new IllegalArgumentException("Company name is required");
        }
        if (companyRepository.existsByName(company.getName())) {
            throw new IllegalArgumentException("Company name already exists");
        }
        if (!StringUtils.hasText(company.getCode())) {
            throw new IllegalArgumentException("Company code is required");
        }
        if (companyRepository.existsByCode(company.getCode())) {
            throw new IllegalArgumentException("Company code already exists");
        }
        if (company.getType() == null) {
            throw new IllegalArgumentException("Company type is required");
        }
    }

    private void validateUpdatedCompany(Long id, Company company) {
        validateRequiredFields(company);

        if (companyRepository.existsByNameAndIdNot(company.getName(), id)) {
            throw new IllegalArgumentException("Company name already exists");
        }

        if (companyRepository.existsByCodeAndIdNot(company.getCode(), id)) {
            throw new IllegalArgumentException("Company code already exists");
        }
    }

    private void validateRequiredFields(Company company) {
        if (company == null) {
            throw new IllegalArgumentException("Company cannot be null");
        }

        if (!StringUtils.hasText(company.getName())) {
            throw new IllegalArgumentException("Company name is required");
        }

        if (!StringUtils.hasText(company.getCode())) {
            throw new IllegalArgumentException("Company code is required");
        }

        if (company.getType() == null) {
            throw new IllegalArgumentException("Company type is required");
        }
    }
}
