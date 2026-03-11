package com.dms.backend.services;

import com.dms.backend.models.Company;
import com.dms.backend.repositories.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    public Company registerCompany(Company company) {
        validateCompany(company);
        return companyRepository.save(company);
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
}
