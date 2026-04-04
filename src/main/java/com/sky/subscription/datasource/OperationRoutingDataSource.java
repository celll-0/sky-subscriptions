package com.sky.subscription.datasource;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Custom routing datasource that determines which database connection to use
 * based on the current operation type in the thread context.
 * 
 * GDPR Compliance: Routes database connections to users with appropriate permissions,
 * ensuring data access is limited to what's necessary for each operation.
 */
@Slf4j
public class OperationRoutingDataSource extends AbstractRoutingDataSource {
    
    @Override
    protected Object determineCurrentLookupKey() {
        OperationType operationType = DataSourceContextHolder.getOperationType();
        String correlationId = MDC.get("correlationId");
        String operationDetails = DataSourceContextHolder.getOperationDetails();
        
        String dbUser = switch (operationType) {
            case READ -> "app_reader";
            case WRITE -> "app";
            case DELETE -> "app_cleaner";
        };
        
        log.debug("[DATASOURCE-ROUTING] Routing to datasource | " +
                "Operation: {} | DB User: {} | Details: {} | Correlation: {}",
                operationType, 
                dbUser,
                operationDetails != null ? operationDetails : "N/A",
                correlationId != null ? correlationId : "N/A");
        
        // Log GDPR-specific events
        if (operationType == OperationType.DELETE && operationDetails != null && operationDetails.contains("customer")) {
            log.info("[GDPR-EVENT] Data deletion request | " +
                    "Operation: {} | DB User: {} | Details: {} | Correlation: {} | " +
                    "Note: Ensure soft delete for GDPR compliance",
                    operationType, dbUser, operationDetails, correlationId);
        }
        
        return operationType;
    }
    
    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        log.info("[DATASOURCE-ROUTING] OperationRoutingDataSource initialized | " +
                "Available datasources: READ (app_reader), WRITE (app), DELETE (app_cleaner)");
    }
}