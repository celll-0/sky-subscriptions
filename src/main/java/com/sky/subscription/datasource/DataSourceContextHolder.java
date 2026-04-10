package com.sky.subscription.datasource;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * Thread-safe context holder for tracking which datasource should be used for the current operation.
 * Uses ThreadLocal to maintain operation context per request thread.
 * 
 * GDPR Compliance: Ensures each request uses the appropriate database user based on operation type,
 * creating a clear audit trail at the database level.
 */
@Slf4j
public class DataSourceContextHolder {
    
    private static final ThreadLocal<OperationType> contextHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> operationDetailsHolder = new ThreadLocal<>();
    
    /**
     * Sets the datasource context for the current thread
     */
    public static void setOperationType(OperationType operationType) {
        if (operationType == null) {
            log.warn("[DATASOURCE] Attempting to set null operation type - clearing context");
            clearContext();
            return;
        }
        
        String correlationId = MDC.get("correlationId");
        String previousType = contextHolder.get() != null ? contextHolder.get().toString() : "NONE"; // TODO: eliminate duplication contextHolder.get() call
        
        contextHolder.set(operationType);
        
        log.info("[DATASOURCE-ROUTING] Setting datasource context | " +
                "Operation: {} | Previous: {} | Thread: {} | Correlation: {} | " +
                "DB User: {} | Permissions: {}",
                operationType, 
                previousType,
                Thread.currentThread().getName(),
                correlationId != null ? correlationId : "N/A",
                getDatabaseUser(operationType),
                getPermissions(operationType));
    }
    
    /**
     * Gets the current operation type for routing
     */
    public static OperationType getOperationType() {
        OperationType type = contextHolder.get();
        if (type == null) {
            log.debug("[DATASOURCE] No operation type set, defaulting to READ for safety");
            return OperationType.READ;  // Default to read-only for safety
        }
        return type;
    }
    
    /**
     * Sets additional details about the current operation for logging
     */
    public static void setOperationDetails(String details) {
        operationDetailsHolder.set(details);
        log.debug("[DATASOURCE-DETAILS] Operation context: {}", details);
    }
    
    /**
     * Gets the current operation details
     */
    public static String getOperationDetails() {
        return operationDetailsHolder.get();
    }
    
    /**
     * Clears the datasource context for the current thread
     */
    public static void clearContext() {
        String previousType = contextHolder.get() != null ? contextHolder.get().toString() : "NONE";
        String correlationId = MDC.get("correlationId");
        
        contextHolder.remove();
        operationDetailsHolder.remove();
        
        log.debug("[DATASOURCE-ROUTING] Cleared datasource context | " +
                "Previous: {} | Thread: {} | Correlation: {}",
                previousType,
                Thread.currentThread().getName(),
                correlationId != null ? correlationId : "N/A");
    }
    
    private static String getDatabaseUser(OperationType type) {
        return switch (type) {
            case READ -> "app_reader";
            case WRITE -> "app";
            case DELETE -> "app_cleaner";
        };
    }
    
    private static String getPermissions(OperationType type) {
        return switch (type) {
            case READ -> "SELECT on all tables";
            case WRITE -> "SELECT, INSERT, UPDATE (excluding direct payment table writes)";
            case DELETE -> "DELETE only on all tables";
        };
    }
}