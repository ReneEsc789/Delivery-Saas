package com.deliverysaas.audit;
import java.time.Instant;import java.util.UUID;import com.deliverysaas.audit.domain.AuditLog;
public record AuditLogResponse(UUID id,UUID userId,String action,String entityType,UUID entityId,String description,String ipAddress,Instant createdAt){public static AuditLogResponse from(AuditLog a){return new AuditLogResponse(a.getId(),a.getUser().getId(),a.getAction(),a.getEntityType(),a.getEntityId(),a.getDescription(),a.getIpAddress(),a.getCreatedAt());}}
