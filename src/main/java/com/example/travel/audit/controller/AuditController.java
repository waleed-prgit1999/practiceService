package com.example.travel.audit.controller;

import com.example.travel.audit.dto.AuditLogResponse;
import com.example.travel.audit.mapper.AuditLogMapper;
import com.example.travel.audit.service.AuditService;
import com.example.travel.auth.security.UserPrincipal;
import com.example.travel.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "View audit trail entries: your own actions, or (admin) every action.")
public class AuditController {

    private final AuditService auditService;
    private final AuditLogMapper auditLogMapper;

    @GetMapping("/me")
    public PageResponse<AuditLogResponse> myAuditTrail(
            @AuthenticationPrincipal UserPrincipal principal, Pageable pageable) {
        return PageResponse.of(auditService.findForUser(principal.getId(), pageable), auditLogMapper::toResponse);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<AuditLogResponse> allAuditLogs(Pageable pageable) {
        return PageResponse.of(auditService.findAll(pageable), auditLogMapper::toResponse);
    }
}
