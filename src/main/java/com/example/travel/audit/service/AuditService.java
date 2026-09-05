package com.example.travel.audit.service;

import com.example.travel.audit.entity.AuditAction;
import com.example.travel.audit.entity.AuditLog;
import com.example.travel.audit.repository.AuditLogRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void record(Long userId, AuditAction action, String entityType, Long entityId, Map<String, Object> metadata) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action.name());
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setMetadata(metadata);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> findForUser(Long userId, Pageable pageable) {
        return auditLogRepository.findAllByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> findAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }
}
