package com.dms.backend.repositories;

import com.dms.backend.models.GeneratedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneratedDocumentRepository extends JpaRepository<GeneratedDocument, Long> {
    List<GeneratedDocument> findAllByGenerationRecordId(Long generationRecordId);
}
