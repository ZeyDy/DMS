package com.dms.backend.repositories;

import com.dms.backend.models.DocumentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Long> {
}
