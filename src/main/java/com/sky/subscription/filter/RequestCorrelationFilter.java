package com.sky.subscription.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that adds a correlation ID to each request for tracing through logs.
 * This correlation ID follows the request through all database operations.
 * 
 * GDPR Compliance: Enables complete audit trail of data access patterns and
 * helps in investigating data breaches or unauthorized access attempts.
 */
@Component
@Order(1)
@Slf4j
public class RequestCorrelationFilter implements Filter {
    
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String CORRELATION_ID_KEY = "correlationId";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Get or generate correlation ID
        String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        
        // Add to MDC for logging
        MDC.put(CORRELATION_ID_KEY, correlationId);
        
        // Add to response header
        httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
        
        // Log request details
        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();
        String remoteAddr = httpRequest.getRemoteAddr();
        
        log.info("[REQUEST-START] {} {} | Query: {} | Client: {} | Correlation: {}",
            method, 
            uri,
            queryString != null ? queryString : "none",
            remoteAddr,
            correlationId);
        
        // Log GDPR-relevant requests
        if (uri.contains("/customers") && method.equals("DELETE")) {
            log.info("[GDPR-REQUEST] Customer deletion request | " +
                    "Method: {} | URI: {} | Correlation: {} | " +
                    "Note: Will trigger soft delete for GDPR compliance",
                    method, uri, correlationId);
        }
        
        if (uri.contains("/customers") && method.equals("GET") && queryString != null) {
            log.info("[GDPR-REQUEST] Customer data access | " +
                    "Method: {} | URI: {} | Query: {} | Correlation: {} | " +
                    "Note: Personal data access logged for GDPR Article 15 compliance",
                    method, uri, queryString, correlationId);
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            chain.doFilter(request, response);
            
            long duration = System.currentTimeMillis() - startTime;
            int status = httpResponse.getStatus();
            
            // Log response
            log.info("[REQUEST-COMPLETE] {} {} | Status: {} | Duration: {}ms | Correlation: {}",
                method,
                uri,
                status,
                duration,
                correlationId);
            
            // Log GDPR compliance for specific responses
            if (uri.contains("/customers") && status == 204) {
                log.info("[GDPR-COMPLIANCE] Customer data operation completed | " +
                        "Status: {} | Correlation: {} | " +
                        "Note: Operation logged for GDPR accountability",
                        status, correlationId);
            }
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            log.error("[REQUEST-ERROR] {} {} | Error: {} | Duration: {}ms | Correlation: {}",
                method,
                uri,
                e.getMessage(),
                duration,
                correlationId);
            
            throw e;
            
        } finally {
            // Clear MDC
            MDC.remove(CORRELATION_ID_KEY);
        }
    }
}