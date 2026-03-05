package main.java.com.dms.backend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.java.com.dms.backend.enums.CompanyType;
import main.java.com.dms.backend.enums.ManagerType;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    private CompanyType type;
    private String name;
    private String code;
    private String category;

    private String address;
    private String cityOrDistrict;

    private ManagerType managerType;
    private String managerFullName;

    private String documentDate;
}
