package com.sky.subscription.aspect;

import com.sky.subscription.datasource.DataSourceContextHolder;
import com.sky.subscription.datasource.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * AOP aspect that intercepts service methods and sets the appropriate datasource context
 * based on the operation type (READ, WRITE, DELETE).
 * 
 * GDPR Compliance: Ensures each operation uses the database user with minimum required
 * permissions, creating a clear audit trail at the database level.
 */
@Aspect
@Component
@Order(1)  // Execute before @Transactional
@Slf4j
public class DataSourceRoutingAspect {
    
    // Pointcuts for different operation types
    @Pointcut("execution(* com.sky.subscription.service.*Service.get*(..)) || " +
              "execution(* com.sky.subscription.service.*Service.find*(..)) || " +
              "execution(* com.sky.subscription.service.*Service.search*(..)) || " +
              "execution(* com.sky.subscription.service.*Service.list*(..))")
    public void readOperations() {}
    
    @Pointcut("execution(* com.sky.subscription.service.*Service.create*(..)) || " +
              "execution(* com.sky.subscription.service.*Service.update*(..)) || " +
              "execution(* com.sky.subscription.service.*Service.save*(..)) || " +
              "execution(* com.sky.subscription.service.*Service.add*(..)) || " +
              "execution(* com.sky.subscription.service.*Service.mark*(..))")
    public void writeOperations() {}
    
    @Pointcut("execution(* com.sky.subscription.service.*Service.delete*(..)) || " +
              "execution(* com.sky.subscription.service.*Service.remove*(..))")
    public void deleteOperations() {}
    
    @Around("readOperations()")
    public Object routeReadOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        return routeOperation(joinPoint, OperationType.READ);
    }
    
    @Around("writeOperations()")
    public Object routeWriteOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        return routeOperation(joinPoint, OperationType.WRITE);
    }
    
    @Around("deleteOperations()")
    public Object routeDeleteOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        return routeOperation(joinPoint, OperationType.DELETE);
    }
    
    private Object routeOperation(ProceedingJoinPoint joinPoint, OperationType operationType) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String correlationId = MDC.get("correlationId");
        Object[] args = joinPoint.getArgs();
        
        // Build operation details
        String operationDetails = String.format("%s.%s(%s)", 
            className, methodName, buildParameterString(args));
        
        // Log operation start
        log.info("[OPERATION-START] {} | Method: {} | Type: {} | Correlation: {} | DB User: {}",
            operationDetails,
            methodName,
            operationType,
            correlationId != null ? correlationId : "N/A",
            getDatabaseUser(operationType));
        
        // Log GDPR compliance for specific operations
        if (operationType == OperationType.DELETE && className.contains("Customer")) {
            log.info("[GDPR-COMPLIANCE] Customer data deletion initiated | " +
                    "Method: {} | Args: {} | DB User: app_cleaner | " +
                    "Note: Soft delete required for GDPR Article 17 compliance",
                    methodName, Arrays.toString(args));
        }
        
        if (operationType == OperationType.READ && className.contains("Payment")) {
            log.info("[GDPR-COMPLIANCE] Payment history access | " +
                    "Method: {} | DB User: app_reader | " +
                    "Permissions: readonly_history role allows payment table access",
                    methodName);
        }
        
        // Set the datasource context
        DataSourceContextHolder.setOperationType(operationType);
        DataSourceContextHolder.setOperationDetails(operationDetails);
        
        // Log database permissions being used
        logDatabasePermissions(operationType, className, methodName);
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // Log successful operation
            log.info("[OPERATION-SUCCESS] {} | Duration: {}ms | Type: {} | DB User: {}",
                operationDetails,
                duration,
                operationType,
                getDatabaseUser(operationType));
            
            // Log result metrics for audit trail
            logResultMetrics(result, operationType, className, methodName);
            
            return result;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            log.error("[OPERATION-FAILURE] {} | Duration: {}ms | Type: {} | Error: {}",
                operationDetails,
                duration,
                operationType,
                e.getMessage());
            
            throw e;
            
        } finally {
            // Clear the context after operation
            DataSourceContextHolder.clearContext();
        }
    }
    
    private void logDatabasePermissions(OperationType type, String className, String methodName) {
        String permissions = switch (type) {
            case READ -> "SELECT on all tables (customer, subscription, package, payment, etc.)";
            case WRITE -> "SELECT, INSERT, UPDATE (excluding direct payment writes)";
            case DELETE -> "DELETE only - no read or write permissions";
        };
        
        String tables = identifyAffectedTables(className, methodName);
        
        log.debug("[DB-PERMISSIONS] Operation: {} | Permissions: {} | Tables: {} | DB User: {}",
            type, permissions, tables, getDatabaseUser(type));
    }
    
    private void logResultMetrics(Object result, OperationType type, String className, String methodName) {
        if (result != null) {
            String metrics = "";
            if (result instanceof java.util.Collection<?> collection) {
                metrics = String.format("Records returned: %d", collection.size());
            } else {
                metrics = "Single record operation";
            }
            
            log.debug("[OPERATION-METRICS] {} | Type: {} | {}", 
                className + "." + methodName, type, metrics);
        }
    }
    
    private String identifyAffectedTables(String className, String methodName) {
        // Map service classes to their primary tables
        if (className.contains("Customer")) return "customer";
        if (className.contains("Subscription")) return "subscription, subscription_add_on, subscription_app";
        if (className.contains("Package")) return "package, package_tier, package_included_app";
        if (className.contains("Payment")) return "payment";
        if (className.contains("AddOn")) return "add_on, subscription_add_on";
        if (className.contains("App")) return "app, app_tier, subscription_app";
        return "multiple tables";
    }
    
    private String getDatabaseUser(OperationType type) {
        return switch (type) {
            case READ -> "app_reader";
            case WRITE -> "app";
            case DELETE -> "app_cleaner";
        };
    }
    
    private String buildParameterString(Object[] args) {
        if (args == null || args.length == 0) {
            return "no parameters";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(args.length, 3); i++) { // Limit to first 3 params
            if (i > 0) sb.append(", ");
            if (args[i] != null) {
                String param = args[i].toString();
                // Truncate long parameters for readability
                if (param.length() > 50) {
                    param = param.substring(0, 47) + "...";
                }
                sb.append(param);
            } else {
                sb.append("null");
            }
        }
        
        if (args.length > 3) {
            sb.append(", ... ").append(args.length - 3).append(" more");
        }
        
        return sb.toString();
    }
}