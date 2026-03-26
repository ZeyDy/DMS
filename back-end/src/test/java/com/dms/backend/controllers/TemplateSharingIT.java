package com.dms.backend.controllers;

import com.dms.backend.enums.Role;
import com.dms.backend.models.Company;
import com.dms.backend.models.DocumentTemplate;
import com.dms.backend.models.User;
import com.dms.backend.repositories.CompanyRepository;
import com.dms.backend.repositories.DocumentTemplateRepository;
import com.dms.backend.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TemplateSharingIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private DocumentTemplateRepository templateRepository;

    private User userA;
    private User userB;
    private DocumentTemplate templateA;

    @BeforeEach
    void setUp() {
        Company company = companyRepository.save(Company.builder()
                .name("Test Company")
                .code("TC01")
                .type(com.dms.backend.enums.CompanyType.UAB)
                .build());

        userA = userRepository.save(User.builder()
                .username("userA")
                .email("a@test.com")
                .role(Role.USER)
                .company(company)
                .build());

        userB = userRepository.save(User.builder()
                .username("userB")
                .email("b@test.com")
                .role(Role.USER)
                .company(company)
                .build());

        templateA = templateRepository.save(DocumentTemplate.builder()
                .name("Template A")
                .fileName("templateA.docx")
                .filePath("/tmp/templateA.docx")
                .subfolder("General")
                .owner(userA)
                .isShared(false)
                .build());
    }

    @Test
    void listTemplates_UserASeesOwnTemplate() throws Exception {
        mockMvc.perform(get("/api/templates")
                        .param("userId", userA.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Template A"));
    }

    @Test
    void listTemplates_UserBCannotSeeUserAsPrivateTemplate() throws Exception {
        mockMvc.perform(get("/api/templates")
                        .param("userId", userB.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void listTemplates_AllUsersCanSeeSharedTemplate() throws Exception {
        templateA.setShared(true);
        templateRepository.save(templateA);

        mockMvc.perform(get("/api/templates")
                        .param("userId", userB.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Template A"));
    }

    @Test
    void toggleSharing_OwnerCanToggle() throws Exception {
        mockMvc.perform(patch("/api/templates/" + templateA.getId() + "/sharing")
                        .param("userId", userA.getId().toString())
                        .param("isShared", "true"))
                .andExpect(status().isOk());

        DocumentTemplate updated = templateRepository.findById(templateA.getId()).get();
        assert(updated.isShared());
    }

    @Test
    void toggleSharing_NonOwnerCannotToggle() throws Exception {
        mockMvc.perform(patch("/api/templates/" + templateA.getId() + "/sharing")
                        .param("userId", userB.getId().toString())
                        .param("isShared", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Only owner or admin can change sharing settings"));
    }

    @Test
    void deleteTemplate_OwnerCanDelete() throws Exception {
        mockMvc.perform(delete("/api/templates/" + templateA.getId())
                        .param("userId", userA.getId().toString()))
                .andExpect(status().isOk());

        assert(templateRepository.findById(templateA.getId()).isEmpty());
    }

    @Test
    void deleteTemplate_NonOwnerCannotDelete() throws Exception {
        mockMvc.perform(delete("/api/templates/" + templateA.getId())
                        .param("userId", userB.getId().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Only owner or admin can delete this template"));
    }
}
