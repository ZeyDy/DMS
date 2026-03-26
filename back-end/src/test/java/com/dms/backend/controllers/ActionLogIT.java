package com.dms.backend.controllers;

import com.dms.backend.enums.ActionLogType;
import com.dms.backend.enums.Role;
import com.dms.backend.models.Company;
import com.dms.backend.models.User;
import com.dms.backend.repositories.CompanyRepository;
import com.dms.backend.repositories.UserRepository;
import com.dms.backend.repositories.ActionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ActionLogIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ActionLogRepository actionLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User admin;

    @BeforeEach
    void setUp() {
        admin = userRepository.save(User.builder()
                .username("admin")
                .email("admin@test.com")
                .role(Role.ADMIN)
                .build());
    }

    @Test
    void companyActionsAreLogged() throws Exception {
        Company company = Company.builder()
                .name("Log Test Co")
                .code("LTC01")
                .type(com.dms.backend.enums.CompanyType.UAB)
                .build();

        // 1. Create Company
        String response = mockMvc.perform(post("/api/companies")
                        .param("userId", admin.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(company)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        
        Long companyId = objectMapper.readTree(response).get("id").asLong();

        // 2. Update Company
        company.setName("Updated Log Test Co");
        mockMvc.perform(put("/api/companies/" + companyId)
                        .param("userId", admin.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(company)))
                .andExpect(status().isOk());

        // 3. Delete Company
        mockMvc.perform(delete("/api/companies/" + companyId)
                        .param("userId", admin.getId().toString()))
                .andExpect(status().isOk());

        // Verify Logs
        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].actionType").value("COMPANY_DELETE"))
                .andExpect(jsonPath("$[1].actionType").value("COMPANY_UPDATE"))
                .andExpect(jsonPath("$[2].actionType").value("COMPANY_CREATE"));
    }
}
