package com.sky.subscription.logging;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Centralized logger for database operations with GDPR compliance tracking.
 * Provides structured logging for audit trails and compliance reporting.
 */
@Component
@Slf4j
public class DatabaseOperationLogger {
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    /**
     * Logs database access operations with full context
     */
    public void logDatabaseAccess(String operation, String table, String dbUser, Object[] params) {
        String correlationId = MDC.get("correlationId");
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        
        log.info("[DB-ACCESS] Operation: {} | Table: {} | DB User: {} | " +
                "Params: {} | Timestamp: {} | Correlation: {}",
                operation,
                table,
                dbUser,
                sanitizeParams(params),
                timestamp,
                correlationId != null ? correlationId : "N/A");
    }
    
    /**
     * Logs GDPR-specific events for compliance tracking
     */
    public void logGDPREvent(GDPREventType eventType, String affectedResource, String operation, String dbUser) {
        String correlationId = MDC.get("correlationId");
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        
        log.info("[GDPR-EVENT] Type: {} | Resource: {} | Operation: {} | " +
                "DB User: {} | Timestamp: {} | Correlation: {} | " +
                "Compliance: {}",
                eventType,
                affectedResource,
                operation,
                dbUser,
                timestamp,
                correlationId != null ? correlationId : "N/A",
                getComplianceNote(eventType));
    }
    
    /**
     * Logs data retention operations
     */
    public void logRetentionOperation(String table, String operation, int recordsAffected, String dbUser) {
        String correlationId = MDC.get("correlationId");
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        
        log.info("[DATA-RETENTION] Table: {} | Operation: {} | Records: {} | " +
                "DB User: {} | Timestamp: {} | Correlation: {} | " +
                "Note: GDPR Article 5(1)(e) - Storage limitation",
                table,
                operation,
                recordsAffected,
                dbUser,
                timestamp,
                correlationId != null ? correlationId : "N/A");
    }
    
    /**
     * Logs permission checks and validations
     */
    public void logPermissionCheck(String resource, String operation, String dbUser, boolean allowed) {
        log.debug("[PERMISSION-CHECK] Resource: {} | Operation: {} | DB User: {} | " +
                "Allowed: {} | Permissions: {}",
                resource,
                operation,
                dbUser,
                allowed,
                getUserPermissions(dbUser));
    }
    
    /**
     * Logs audit trail events
     */
    public void logAuditEvent(String entity, String operation, String oldValue, String newValue, String dbUser) {
        String correlationId = MDC.get("correlationId");
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        
        log.info("[AUDIT-TRAIL] Entity: {} | Operation: {} | " +
                "Old: {} | New: {} | DB User: {} | " +
                "Timestamp: {} | Correlation: {}",
                entity,
                operation,
                sanitizeValue(oldValue),
                sanitizeValue(newValue),
                dbUser,
                timestamp,
                correlationId != null ? correlationId : "N/A");
    }
    
    /**
     * Logs security events
     */
    public void logSecurityEvent(SecurityEventType eventType, String details, String dbUser) {
        String correlationId = MDC.get("correlationId");
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        
        log.warn("[SECURITY-EVENT] Type: {} | Details: {} | DB User: {} | " +
                "Timestamp: {} | Correlation: {} | " +
                "Action: Monitor for potential security issues",
                eventType,
                details,
                dbUser,
                timestamp,
                correlationId != null ? correlationId : "N/A");
    }
    
    private String sanitizeParams(Object[] params) {
        if (params == null || params.length == 0) {
            return "none";
        }
        
        // Mask sensitive data in parameters
        return Arrays.stream(params)
            .map(this::sanitizeValue)
            .reduce((a, b) -> a + ", " + b)
            .orElse("");
    }
    
    private String sanitizeValue(Object value) {
        if (value == null) {
            return "null";
        }
        
        String str = value.toString();
        
        // Mask email addresses
        if (str.contains("@")) {
            int atIndex = str.indexOf('@');
            if (atIndex > 2) {
                return str.substring(0, 2) + "***" + str.substring(atIndex);
            }
        }
        
        // Truncate long values
        if (str.length() > 100) {
            return str.substring(0, 97) + "...";
        }
        
        return str;
    }
    
    private String getComplianceNote(GDPREventType eventType) {
        return switch (eventType) {
            case DATA_ACCESS -> "Article 15 - Right of access";
            case DATA_ERASURE -> "Article 17 - Right to erasure";
            case DATA_PORTABILITY -> "Article 20 - Right to data portability";
            case DATA_RECTIFICATION -> "Article 16 - Right to rectification";
            case CONSENT_GIVEN -> "Article 7 - Conditions for consent";
            case CONSENT_WITHDRAWN -> "Article 7(3) - Withdrawal of consent";
            case DATA_BREACH -> "Articles 33-34 - Breach notification";
        };
    }
    
    private String getUserPermissions(String dbUser) {
        return switch (dbUser) {
            case "app_reader" -> "SELECT on all tables (readonly + readonly_history)";
            case "app" -> "SELECT, INSERT, UPDATE (main + history_manager)";
            case "app_cleaner" -> "DELETE only (deleteonly_admin)";
            default -> "unknown";
        };
    }
    
    public enum GDPREventType {
        DATA_ACCESS,
        DATA_ERASURE,
        DATA_PORTABILITY,
        DATA_RECTIFICATION,
        CONSENT_GIVEN,
        CONSENT_WITHDRAWN,
        DATA_BREACH
    }
    
    public enum SecurityEventType {
        UNAUTHORIZED_ACCESS,
        SUSPICIOUS_ACTIVITY,
        RATE_LIMIT_EXCEEDED,
        INVALID_CREDENTIALS,
        DATA_EXPORT,
        BULK_OPERATION
    }
}