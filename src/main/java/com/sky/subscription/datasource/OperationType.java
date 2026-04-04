package com.sky.subscription.datasource;

/**
 * Defines the type of database operation being performed.
 * Each operation type maps to a specific database user with restricted permissions.
 * 
 * GDPR Compliance: This segregation ensures principle of least privilege and
 * creates auditable boundaries between different data access patterns.
 */
public enum OperationType {
    /**
     * Read-only operations (SELECT)
     * Database User: app_reader
     * Roles: readonly + readonly_history
     * Permissions: SELECT on all tables including payment history
     */
    READ,
    
    /**
     * Write operations (INSERT, UPDATE)
     * Database User: app
     * Roles: main + history_manager
     * Permissions: SELECT, INSERT, UPDATE on all tables
     */
    WRITE,
    
    /**
     * Delete operations (DELETE)
     * Database User: app_cleaner
     * Roles: deleteonly_admin
     * Permissions: DELETE only on all tables
     * GDPR Note: Used for data erasure requests and retention policy enforcement
     */
    DELETE
}