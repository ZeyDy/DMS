package com.dms.backend.services;

import com.dms.backend.enums.ActionLogType;
import com.dms.backend.models.ActionLog;
import com.dms.backend.models.User;
import com.dms.backend.repositories.ActionLogRepository;
import com.dms.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActionLogService {

    private final ActionLogRepository actionLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public void logAction(Long userId, ActionLogType actionType, Long entityId, String entityType, String details) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        ActionLog log = ActionLog.builder()
                .user(user)
                .actionType(actionType)
                .entityId(entityId)
                .entityType(entityType)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        actionLogRepository.save(log);
    }

    public List<ActionLog> getAllLogs() {
        return actionLogRepository.findAllByOrderByIdDesc();
    }
}
