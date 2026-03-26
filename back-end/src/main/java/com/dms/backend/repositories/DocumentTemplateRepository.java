package com.dms.backend.repositories;

import com.dms.backend.models.DocumentTemplate;
import com.dms.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Long> {
    Optional<DocumentTemplate> findByFilePath(String filePath);
    List<DocumentTemplate> findAllBySubfolderIn(List<String> subfolders);
    List<DocumentTemplate> findByOwnerOrIsSharedTrue(User owner);
}
