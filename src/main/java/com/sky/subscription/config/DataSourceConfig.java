package com.sky.subscription.config;

import com.sky.subscription.datasource.OperationType;
import com.sky.subscription.datasource.OperationRoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for multiple datasources based on operation type and database user permissions.
 * 
 * GDPR Compliance: Implements role-based database access control at the connection level.
 * Each datasource connects with different database credentials matching the principle of least privilege.
 */
@Configuration
@Slf4j
public class DataSourceConfig {
    
    // Reader DataSource Configuration (app_reader user)
    @Bean
    @ConfigurationProperties("spring.datasource.reader")
    public DataSourceProperties readerDataSourceProperties() {
        log.info("[DATASOURCE-CONFIG] Configuring READER datasource (app_reader) - readonly permissions");
        return new DataSourceProperties();
    }
    
    @Bean
    @ConfigurationProperties("spring.datasource.reader.hikari")
    public DataSource readerDataSource() {
        DataSourceProperties properties = readerDataSourceProperties();

        log.info("[DATASOURCE-CONFIG] READER datasource configured | User: {} | URL: {}",
                properties.getUsername(), properties.getUrl());

        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }
    
    // Writer DataSource Configuration (app user)
    @Bean
    @ConfigurationProperties("spring.datasource.writer")
    public DataSourceProperties writerDataSourceProperties() {
        log.info("[DATASOURCE-CONFIG] Configuring WRITER datasource (app) - main operations");
        return new DataSourceProperties();
    }
    
    @Bean
    @ConfigurationProperties("spring.datasource.writer.hikari")
    public DataSource writerDataSource() {
        DataSourceProperties properties = writerDataSourceProperties();

        log.info("[DATASOURCE-CONFIG] WRITER datasource configured | User: {} | URL: {}",
                properties.getUsername(), properties.getUrl());

        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }
    
    // Deleter DataSource Configuration (app_cleaner user)
    @Bean
    @ConfigurationProperties("spring.datasource.deleter")
    public DataSourceProperties deleterDataSourceProperties() {
        log.info("[DATASOURCE-CONFIG] Configuring DELETER datasource (app_cleaner) - deletion only");
        return new DataSourceProperties();
    }
    
    @Bean
    @ConfigurationProperties("spring.datasource.deleter.hikari")
    public DataSource deleterDataSource() {
        DataSourceProperties properties = deleterDataSourceProperties();

        log.info("[DATASOURCE-CONFIG] DELETER datasource configured | User: {} | URL: {}",
                properties.getUsername(), properties.getUrl());

        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }
    
    // Routing DataSource Configuration
    @Bean
    @Primary
    public DataSource routingDataSource(
            @Qualifier("readerDataSource") DataSource readerDataSource,
            @Qualifier("writerDataSource") DataSource writerDataSource,
            @Qualifier("deleterDataSource") DataSource deleterDataSource) {
        
        log.info("[DATASOURCE-CONFIG] Configuring routing datasource for operation-based connection management");
        
        OperationRoutingDataSource routingDataSource = new OperationRoutingDataSource();
        
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(OperationType.READ, readerDataSource);
        dataSourceMap.put(OperationType.WRITE, writerDataSource);
        dataSourceMap.put(OperationType.DELETE, deleterDataSource);
        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(readerDataSource); // Default to read-only for safety
        
        log.info("[DATASOURCE-CONFIG] Routing datasource configured | " +
                "Default: READ (app_reader) | " +
                "Available routes: READ -> app_reader, WRITE -> app, DELETE -> app_cleaner | " +
                "GDPR: Role-based access control enforced at database level");
        
        return routingDataSource;
    }
}
