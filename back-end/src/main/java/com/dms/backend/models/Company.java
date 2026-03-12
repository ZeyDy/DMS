package com.dms.backend.models;

import main.java.com.dms.backend.enums.CompanyType;
import main.java.com.dms.backend.enums.ManagerType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyType type;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    private String category;

    private String address;

    private String cityOrDistrict;

    @Enumerated(EnumType.STRING)
    private ManagerType managerType;

    private String managerFullName;

    private String documentDate;
}
