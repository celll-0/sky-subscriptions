# Sky Subscription API - GDPR-Compliant Multi-DataSource Implementation

## Overview
A Spring Boot REST API for managing Sky UK subscription packages with **role-based database access control** and **comprehensive GDPR compliance logging**. The application uses three separate database connections with different permission levels to ensure principle of least privilege at the database level.

## Key Features

### Multi-DataSource Architecture
- **Three Database Users with Segregated Permissions**:
  - `app_reader`: Read-only operations (SELECT on all tables)
  - `app`: Primary operations (SELECT, INSERT, UPDATE)
  - `app_cleaner`: Delete operations only (DELETE on all tables)

### GDPR Compliance Features
- **Complete audit trail** at database level showing which user performed each operation
- **Request correlation IDs** for tracking operations across all layers
- **Soft delete** for customer data (Article 17 compliance)
- **Detailed operation logging** with GDPR event tracking
- **Role-based access control** enforced at database level

## Architecture

### Database Access Flow
```
HTTP Request → Controller → Service (AOP Aspect) → Repository → Database
                               ↓
                    DataSource Routing Logic
                               ↓
                 [app_reader | app | app_cleaner]
```

### Operation Type Detection
The AOP aspect automatically detects operation types based on method names:
- **READ**: `get*`, `find*`, `search*`, `list*` → Uses `app_reader`
- **WRITE**: `create*`, `update*`, `save*`, `add*`, `mark*` → Uses `app`
- **DELETE**: `delete*`, `remove*` → Uses `app_cleaner`

## Configuration

### Environment Variables
Set the following environment variables for database credentials:

```bash
# Reader DataSource (app_reader user)
DB_READER_USERNAME=app_reader
DB_READER_PASSWORD=your_reader_password

# Writer DataSource (app user)
DB_WRITER_USERNAME=app
DB_WRITER_PASSWORD=your_writer_password

# Deleter DataSource (app_cleaner user)
DB_DELETER_USERNAME=app_cleaner
DB_DELETER_PASSWORD=your_cleaner_password
```

### Application Properties
The application automatically configures three connection pools with appropriate sizes:
- Reader Pool: 5 connections (for read operations)
- Writer Pool: 10 connections (for main operations)
- Deleter Pool: 3 connections (for deletion operations)

## Running the Application

### Prerequisites
1. PostgreSQL database with the Sky subscription schema
2. Three database users created with appropriate roles:
   ```sql
   -- app_reader with readonly + readonly_history roles
   -- app with main + history_manager roles
   -- app_cleaner with deleteonly_admin role
   ```
3. Java 17 or higher

### Build and Run
```bash
# Build the application
./gradlew build

# Run with environment variables
DB_READER_USERNAME=app_reader \
DB_READER_PASSWORD=pass1 \
DB_WRITER_USERNAME=app \
DB_WRITER_PASSWORD=pass2 \
DB_DELETER_USERNAME=app_cleaner \
DB_DELETER_PASSWORD=pass3 \
./gradlew bootRun
```

## Logging and Monitoring

### Log Levels
The application provides detailed logging at different levels:
- **INFO**: Operation start/complete, GDPR events, routing decisions
- **DEBUG**: Detailed permission checks, context changes
- **TRACE**: SQL queries with bound parameters

### Example Log Output
```
2024-04-03 20:30:15.123 [http-nio-8080-exec-1] [abc-123] INFO RequestCorrelationFilter - [REQUEST-START] GET /api/customers/42 | Client: 192.168.1.100 | Correlation: abc-123

2024-04-03 20:30:15.124 [http-nio-8080-exec-1] [abc-123] INFO DataSourceRoutingAspect - [OPERATION-START] CustomerService.getCustomerById(42) | Type: READ | Correlation: abc-123 | DB User: app_reader

2024-04-03 20:30:15.125 [http-nio-8080-exec-1] [abc-123] DEBUG DataSourceContextHolder - [DATASOURCE-ROUTING] Setting datasource context | Operation: READ | DB User: app_reader | Permissions: SELECT on all tables

2024-04-03 20:30:15.150 [http-nio-8080-exec-1] [abc-123] INFO DataSourceRoutingAspect - [OPERATION-SUCCESS] CustomerService.getCustomerById(42) | Duration: 25ms | Type: READ | DB User: app_reader

2024-04-03 20:30:15.151 [http-nio-8080-exec-1] [abc-123] INFO RequestCorrelationFilter - [REQUEST-COMPLETE] GET /api/customers/42 | Status: 200 | Duration: 28ms | Correlation: abc-123
```

### GDPR-Specific Logging
Special events are logged for GDPR compliance:
```
[GDPR-EVENT] Type: DATA_ACCESS | Resource: customer/42 | Operation: READ | DB User: app_reader | Compliance: Article 15 - Right of access

[GDPR-COMPLIANCE] Customer data deletion initiated | Method: deleteCustomer | DB User: app_cleaner | Note: Soft delete required for GDPR Article 17 compliance
```

## API Endpoints

### Customers
- `GET /api/customers` - List all customers (app_reader)
- `GET /api/customers/{id}` - Get customer by ID (app_reader)
- `POST /api/customers` - Create customer (app)
- `PUT /api/customers/{id}` - Update customer (app)
- `DELETE /api/customers/{id}` - Delete customer (app_cleaner)

### Subscriptions
- `GET /api/subscriptions` - List all subscriptions (app_reader)
- `POST /api/subscriptions` - Create subscription (app)
- `PUT /api/subscriptions/{id}` - Update subscription (app)
- `DELETE /api/subscriptions/{id}` - Delete subscription (app_cleaner)

### Payments
- `GET /api/payments` - List payments (app_reader with readonly_history)
- `POST /api/payments` - Create payment (app with history_manager)
- `PATCH /api/payments/{id}/mark-paid` - Mark as paid (app)

## Testing the Multi-DataSource Routing

### Verify Operation Routing
1. Enable DEBUG logging for datasource package:
   ```yaml
   logging.level.com.sky.subscription.datasource: DEBUG
   ```

2. Make API calls and observe which database user is selected:
   ```bash
   # READ operation - should use app_reader
   curl http://localhost:8080/api/customers
   
   # WRITE operation - should use app
   curl -X POST http://localhost:8080/api/customers -H "Content-Type: application/json" -d '{...}'
   
   # DELETE operation - should use app_cleaner
   curl -X DELETE http://localhost:8080/api/customers/1
   ```

3. Check logs to confirm correct datasource routing

### Database Audit
Connect to PostgreSQL and check the audit logs:
```sql
-- Check which user performed recent operations
SELECT usename, query, query_start 
FROM pg_stat_activity 
WHERE datname = 'sky_subscription'
ORDER BY query_start DESC;
```

## Security Considerations

1. **Credential Storage**: In production, use a secure vault (HashiCorp Vault, AWS Secrets Manager) instead of environment variables
2. **Network Security**: Ensure database connections use SSL/TLS
3. **Monitoring**: Set up alerts for failed authentication attempts or unusual access patterns
4. **Rotation**: Implement regular credential rotation for all three database users

## Compliance Notes

### GDPR Articles Addressed
- **Article 5(2)**: Accountability - Complete audit trail at database level
- **Article 15**: Right of access - Logged and tracked
- **Article 17**: Right to erasure - Soft delete implementation
- **Article 25**: Data protection by design - Role-based access control
- **Article 32**: Security of processing - Principle of least privilege

### Audit Trail
All database operations are logged with:
- Correlation ID for request tracing
- Database user performing the operation
- Timestamp and duration
- Operation type and affected resources
- GDPR compliance notes where applicable

## Troubleshooting

### Common Issues

1. **Connection Pool Exhaustion**
   - Check pool sizes in `application.yml`
   - Monitor active connections per pool
   - Adjust based on load patterns

2. **Permission Denied Errors**
   - Verify database user roles are correctly assigned
   - Check that routing logic is selecting correct datasource
   - Review operation type detection in AOP aspect

3. **Transaction Issues**
   - Ensure transactions don't span multiple datasources
   - Check that `@Transactional` annotations work with routing

## Future Enhancements

1. **Audit Triggers**: Add database triggers for additional audit logging
2. **Read Replicas**: Route read operations to read-only replicas
3. **Caching**: Implement caching layer for frequently accessed data
4. **Rate Limiting**: Add per-user rate limiting for GDPR compliance
5. **Data Masking**: Implement field-level encryption for sensitive data

## License
Internal use only - Sky UK

## Support
For issues or questions, contact the development team.