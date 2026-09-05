package com.example.travel.audit.mapper;

import com.example.travel.audit.dto.AuditLogResponse;
import com.example.travel.audit.entity.AuditLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    AuditLogResponse toResponse(AuditLog auditLog);
}
