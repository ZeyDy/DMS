package com.dms.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class GenerateByAreasRequest {
    private Long companyId;
    private List<String> selectedAreas;
}