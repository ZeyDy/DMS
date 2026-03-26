package com.dms.backend.controllers;

import com.dms.backend.enums.CompanyType;
import com.dms.backend.models.Company;
import com.dms.backend.services.CompanyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyController.class)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompanyService companyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser // Simuliuoja prisijungusį vartotoją
    void registerCompany_ShouldReturnSavedCompany() throws Exception {
        // Paruošiami duomenys
        Company inputCompany = Company.builder()
                .name("Test Company")
                .code("123456")
                .type(CompanyType.UAB) // Daroma prielaida, kad toks Enum egzistuoja
                .build();

        Company savedCompany = Company.builder()
                .id(1L)
                .name("Test Company")
                .code("123456")
                .type(CompanyType.UAB)
                .build();

        // Mock'uojamas serviso elgesys
        when(companyService.registerCompany(any(Company.class), any())).thenReturn(savedCompany);

        // Vykdomas testas
        mockMvc.perform(post("/api/companies")
                        .with(csrf()) // Pridedamas CSRF tokenas (reikalinga Spring Security)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputCompany)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Company"))
                .andExpect(jsonPath("$.code").value("123456"));
    }
    @Test
    @WithMockUser
    void searchCompanies_ShouldReturnMatchingCompanies() throws Exception {
        // Paruošiami duomenys
        Company company1 = Company.builder()
                .id(1L)
                .name("Apple Inc")
                .code("111")
                .type(CompanyType.UAB)
                .build();

        Company company2 = Company.builder()
                .id(2L)
                .name("Pineapple Corp")
                .code("222")
                .type(CompanyType.UAB)
                .build();

        when(companyService.searchCompaniesByName("apple")).thenReturn(List.of(company1, company2));

        // Vykdomas testas
        mockMvc.perform(get("/api/companies/search")
                        .param("name", "apple")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Apple Inc"))
                .andExpect(jsonPath("$[1].name").value("Pineapple Corp"));
    }
}