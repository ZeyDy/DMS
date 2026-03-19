package com.dms.backend.repositories;

import com.dms.backend.models.GenerationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenerationRecordRepository extends JpaRepository<GenerationRecord, Long> {
}
