package com.dms.backend.controllers;

import com.dms.backend.models.ActionLog;
import com.dms.backend.services.ActionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class ActionLogController {

    private final ActionLogService actionLogService;

    @GetMapping
    public List<ActionLog> getAllLogs() {
        return actionLogService.getAllLogs();
    }
}
