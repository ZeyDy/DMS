package com.dms.backend.services;

import com.dms.backend.models.Company;
import com.dms.backend.repositories.CompanyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyService companyService;

    @Test
    void searchCompaniesByName_ShouldReturnMatchingCompanies() {
        // Given
        String query = "test";
        Company company1 = Company.builder().name("Test Company").build();
        Company company2 = Company.builder().name("Another test").build();
        when(companyRepository.findByNameContainingIgnoreCase(query)).thenReturn(List.of(company1, company2));

        // When
        List<Company> results = companyService.searchCompaniesByName(query);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).containsExactly(company1, company2);
    }

    @Test
    void searchCompaniesByName_ShouldReturnEmptyList_WhenNameIsBlank() {
        // When
        List<Company> results = companyService.searchCompaniesByName("");

        // Then
        assertThat(results).isEmpty();
    }
}
