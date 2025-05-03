## CommonLibraryApplication.java

```java
package io.joshuasalcedo.commonlibs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommonLibraryApplication {

    public static void main(String[] args) {

        SpringApplication.run(CommonLibraryApplication.class, args);
    }
}
```

## EntityUtil.java

```java
package io.joshuasalcedo.commonlibs;

/**
 * Utility class for entity operations.
 */
public class EntityUtil {

    /**
     * Checks if the given object is null.
     *
     * @param object the object to check
     * @return true if the object is null, false otherwise
     */
    public boolean isNull(Object object) {
        return object == null;
    }

    /**
     * Checks if the given string is null or empty.
     *
     * @param str the string to check
     * @return true if the string is null or empty, false otherwise
     */
    public boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}```

## BannerAutoConfiguration.java

```java
package io.joshuasalcedo.commonlibs.autoconfigure;

import io.joshuasalcedo.commonlibs.implementation.CustomBanner;
import io.joshuasalcedo.commonlibs.properties.BannerProperties;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
@AutoConfiguration
@AutoConfigurationPackage
@EnableConfigurationProperties(BannerProperties.class)
@ConditionalOnProperty(prefix = "io.joshuasalcedo.common.banner", name = "enabled", matchIfMissing = true)
public class BannerAutoConfiguration {

    private final BannerProperties properties;

    public BannerAutoConfiguration(BannerProperties properties) {
        this.properties = properties;
    }

    @Bean
    public Banner customBanner() {
        return new CustomBanner(this.properties);
    }
}```

## ControllerAutoConfiguration.java

```java
package io.joshuasalcedo.commonlibs.autoconfigure;

import io.joshuasalcedo.commonlibs.domain.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "io.joshuasalcedo.common.controller", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ControllerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}```

## DataSourceProxyConfiguration.java

```java
package io.joshuasalcedo.commonlibs.autoconfigure;

import io.joshuasalcedo.commonlibs.properties.LoggingProperties;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Configuration to set up datasource proxy for SQL query logging.
 */
@Configuration
@ConditionalOnClass(name = "net.ttddyy.dsproxy.support.ProxyDataSourceBuilder")
@ConditionalOnProperty(prefix = "io.joshuasalcedo.logging", name = "sql-logging", havingValue = "true")
public class DataSourceProxyConfiguration {

    /**
     * Create a proxy around the existing DataSource to intercept
     * SQL queries for logging.
     */
    @Bean
    @Primary
    public DataSource proxyDataSource(DataSource dataSource, 
                                     QueryExecutionListener queryExecutionListener,
                                     LoggingProperties properties) {
        
        if (!properties.isSqlLogging()) {
            return dataSource;
        }
        
        return ProxyDataSourceBuilder
                .create(dataSource)
                .name("SQL-Query-Logger")
                .listener(queryExecutionListener)
                .build();
    }
}```

## EntityAutoConfiguration.java

```java
package io.joshuasalcedo.commonlibs.autoconfigure;

import io.joshuasalcedo.commonlibs.EntityUtil;
import io.joshuasalcedo.commonlibs.listeners.AuditingEntityListener;
import io.joshuasalcedo.commonlibs.properties.DomainProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * Auto-configuration for entity functionality.
 */
@Configuration
@EnableConfigurationProperties(DomainProperties.class)
public class EntityAutoConfiguration {

    private final DomainProperties properties;

    public EntityAutoConfiguration(DomainProperties properties) {
        this.properties = properties;
    }

    /**
     * Create and configure an AuditingEntityListener bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public AuditingEntityListener auditingEntityListener() {
        AuditingEntityListener listener = new AuditingEntityListener();
        listener.setEntityProperties(properties.getEntity());
        return listener;
    }

    /**
     * Configuration for JPA auditing.
     */
    @Configuration
    @ConditionalOnProperty(prefix = "io.joshuasalcedo.common.domain.entity", name = "enable-auditing", havingValue = "true", matchIfMissing = true)
    @EnableJpaAuditing(auditorAwareRef = "auditorProvider")
    public static class AuditingConfiguration {

        private final DomainProperties properties;

        public AuditingConfiguration(DomainProperties properties) {
            this.properties = properties;
        }

        /**
         * Provides the current auditor for JPA auditing.
         */
        @Bean
        @ConditionalOnMissingBean
        public AuditorAware<String> auditorProvider() {
            return () -> Optional.of(properties.getEntity().getSystemUsername());
        }
    }

    /**
     * Create an EntityUtil bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public EntityUtil entityUtil() {
        return new EntityUtil();
    }
}```

## EntityLifecycleAutoConfiguration.java

```java
package io.joshuasalcedo.commonlibs.autoconfigure;

import io.joshuasalcedo.commonlibs.domain.listeners.EntityLifecycleListener;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({EntityManagerFactory.class, EntityLifecycleListener.class})
@ConditionalOnProperty(prefix = "io.joshuasalcedo.common.lifecycle", name = "lifecycle-events-enabled", havingValue = "true", matchIfMissing = true)
public class EntityLifecycleAutoConfiguration {

    @Bean
    public EntityLifecycleListener entityLifecycleListener() {
        return new EntityLifecycleListener();
    }
}```

## LoggingAutoConfiguration.java

```java
package io.joshuasalcedo.commonlibs.autoconfigure;


import io.joshuasalcedo.commonlibs.domain.logging.LoggingAspect;
import io.joshuasalcedo.commonlibs.domain.logging.LoggingFactory;
import io.joshuasalcedo.commonlibs.domain.logging.LoggingManager;
import io.joshuasalcedo.commonlibs.domain.logging.LoggingService;
import io.joshuasalcedo.commonlibs.properties.LoggingProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Auto-configuration for the logging system.
 */
@Configuration
@EnableConfigurationProperties(LoggingProperties.class)
@ConditionalOnProperty(prefix = "io.joshuasalcedo.common.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableAspectJAutoProxy

public class LoggingAutoConfiguration {

    private final LoggingProperties properties;

    public LoggingAutoConfiguration(LoggingProperties properties) {
        this.properties = properties;
    }

    /**
     * Create the LoggingFactory bean and initialize the LoggingManager.
     */
    @Bean
    @ConditionalOnMissingBean
    public LoggingFactory loggingFactory() {
        LoggingFactory factory = new LoggingFactory(properties);
        LoggingManager.setLoggingFactory(factory);
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public LoggingService defaultLoggingService(LoggingFactory loggingFactory) {
        return loggingFactory.getLogger(LoggingAutoConfiguration.class);
    }

    /**
     * Create the LoggingAspect bean for AOP logging.
     */
    @Bean
    @ConditionalOnMissingBean
    public LoggingAspect loggingAspect(LoggingFactory loggingFactory) {
        return new LoggingAspect(loggingFactory, properties);
    }

    /**
     * Create a CommonsRequestLoggingFilter bean for HTTP request logging.
     */
    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnProperty(prefix = "io.joshuasalcedo.common.logging", name = "request-logging", havingValue = "true", matchIfMissing = true)
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST DATA: ");
        return filter;
    }

}```

## RepositoryAutoConfiguration.java

```java
package io.joshuasalcedo.commonlibs.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;


/**
 * Auto-configuration for repository components.
 * This configuration enables JPA repositories and entity scanning.
 */
@Configuration
@ConditionalOnProperty(prefix = "io.joshuasalcedo.common.repository", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RepositoryAutoConfiguration {

    /**
     * Repository configuration that allows applications to define their own repository packages.
     * This configuration doesn't force any specific package structure on the application.
     */
    @Configuration
    public static class DefaultRepositoryConfiguration {
        // This is intentionally left empty to allow Spring Boot's auto-configuration
        // to set up repositories based on the application's own configuration
    }
}```

## ServiceAutoConfiguration.java

```java
package io.joshuasalcedo.commonlibs.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ConditionalOnProperty(prefix = "io.joshuasalcedo.common.service", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ServiceAutoConfiguration {
    // Service configuration here
}```

## GlobalExceptionHandler.java

```java
package io.joshuasalcedo.commonlibs.domain;

import io.joshuasalcedo.commonlibs.domain.base.dto.ResponseDTO;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle ResourceNotFoundException.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseDTO<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new ResponseEntity<>(ResponseDTO.error(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    /**
     * Handle validation exceptions.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDTO<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        ResponseDTO<Map<String, String>> response = ResponseDTO.error("Validation failed");
        response.setData(errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle constraint violation exceptions.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseDTO<Map<String, String>>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                errors.put(violation.getPropertyPath().toString(), violation.getMessage()));

        ResponseDTO<Map<String, String>> response = ResponseDTO.error("Validation failed");
        response.setData(errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO<Object>> handleGenericException(Exception ex) {
        return new ResponseEntity<>(
                ResponseDTO.error("An error occurred: " + ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}```

## ResourceNotFoundException.java

```java
package io.joshuasalcedo.commonlibs.domain;

/**
 * Exception thrown when a requested resource cannot be found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}```

## BaseEntity.java

```java
package io.joshuasalcedo.commonlibs.domain.base;

import io.joshuasalcedo.commonlibs.domain.listeners.EntityLifecycleListener;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Base abstract entity class that provides common fields and functionality
 * for all domain entities in the application.
 *
 * @param <ID> The type of the entity's primary key
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class, EntityLifecycleListener.class})
public abstract class BaseEntity<ID extends Serializable> implements Serializable {



    @Serial
    @Builder.Default
    private static final long serialVersionUID = 1L;

    /**
     * Unique business identifier for the entity that remains consistent
     * across systems and is safe to expose externally.
     */
    @Column(nullable = false, unique = true)
    private String uuid;


    /**
     * Version field for optimistic locking to prevent concurrent modifications.
     */
    @Version
    @Column(name = "version")
    @Builder.Default  // Add this annotation
    private Long version = 0L;

    /**
     * Flag indicating if the entity is active.
     * Used for soft deletion.
     */
    @Column(name = "active")
    @Builder.Default  // Add this annotation
    private boolean active = true;

    /**
     * Timestamp when the entity was created.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Identifier of the user who created the entity.
     */
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    /**
     * Timestamp when the entity was last updated.
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Identifier of the user who last updated the entity.
     */
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    /**
     * Returns the ID of the entity. Must be implemented by subclasses.
     *
     * @return the ID of the entity
     */
    public abstract ID getId();

    /**
     * Sets the ID of the entity. Must be implemented by subclasses.
     *
     * @param id the ID to set
     */
    public abstract void setId(ID id);

    /**
     * Marker method to check if the entity is new (not yet persisted).
     *
     * @return true if the entity is new, false otherwise
     */
    @Transient
    public boolean isNew() {
        return getId() == null;
    }

    /**
     * Pre-persist lifecycle callback to set initial values before persisting.
     */
    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = createdAt;
    }

    /**
     * Pre-update lifecycle callback to update timestamps before updating.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Method to soft delete an entity by setting active to false.
     */
    public void softDelete() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity<?> that = (BaseEntity<?>) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}```

## BaseRepository.java

```java
package io.joshuasalcedo.commonlibs.domain.base;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * Base repository interface that extends standard Spring Data JPA repository interfaces.
 * This provides common CRUD operations plus specification support for complex queries.
 *
 * @param <T> the entity type
 * @param <ID> the type of the entity's primary key
 */
@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity<ID>, ID extends Serializable>
        extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    /**
     * Find an entity by its UUID.
     *
     * @param uuid the UUID to search for
     * @return the entity with the given UUID, or null if none found
     */
    T findByUuid(String uuid);
}```

## BaseController.java

```java
package io.joshuasalcedo.commonlibs.domain.base.controller;

import io.joshuasalcedo.commonlibs.domain.base.BaseEntity;
import io.joshuasalcedo.commonlibs.domain.base.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
 * Base controller interface that defines standard REST endpoints for CRUD operations.
 *
 * @param <T> the entity type
 * @param <ID> the type of the entity's primary key
 */
@Tag(name = "Base Controller", description = "Standard CRUD operations")
public interface BaseController<T extends BaseEntity<ID>, ID extends Serializable> {

    @Operation(summary = "Create a new entity", description = "Creates a new entity with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Entity created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    ResponseEntity<ResponseDTO<T>> create(@RequestBody T entity);

    @Operation(summary = "Get entity by ID", description = "Retrieves an entity by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Entity found"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ResponseDTO<T>> findById(
            @Parameter(description = "ID of the entity to retrieve") @PathVariable ID id);

    @Operation(summary = "Get entity by UUID", description = "Retrieves an entity by its UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Entity found"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @GetMapping("/uuid/{uuid}")
    ResponseEntity<ResponseDTO<T>> findByUuid(
            @Parameter(description = "UUID of the entity to retrieve") @PathVariable String uuid);

    @Operation(summary = "List all entities", description = "Retrieves all entities")
    @ApiResponse(responseCode = "200", description = "List of entities")
    @GetMapping
    ResponseEntity<ResponseDTO<List<T>>> findAll();

    @Operation(summary = "List all entities (paginated)", description = "Retrieves entities with pagination")
    @ApiResponse(responseCode = "200", description = "Page of entities")
    @GetMapping("/page")
    ResponseEntity<ResponseDTO<Page<T>>> findAll(Pageable pageable);

    @Operation(summary = "Update entity by ID", description = "Updates an existing entity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Entity updated successfully"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    ResponseEntity<ResponseDTO<T>> update(
            @Parameter(description = "ID of the entity to update") @PathVariable ID id,
            @RequestBody T entity);

    @Operation(summary = "Delete entity by ID", description = "Permanently deletes an entity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Entity deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ResponseDTO<Void>> deleteById(
            @Parameter(description = "ID of the entity to delete") @PathVariable ID id);
}```

## CrudBaseController.java

```java
package io.joshuasalcedo.commonlibs.domain.base.controller;

import io.joshuasalcedo.commonlibs.domain.base.BaseEntity;
import io.joshuasalcedo.commonlibs.domain.ResourceNotFoundException;
import io.joshuasalcedo.commonlibs.domain.base.dto.ResponseDTO;
import io.joshuasalcedo.commonlibs.domain.base.service.BaseService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.Serializable;
import java.util.List;

/**
 * Base implementation of BaseController that provides standard REST endpoints.
 *
 * @param <T> the entity type
 * @param <S> the service type
 * @param <ID> the type of the entity's primary key
 */
public class CrudBaseController<T extends BaseEntity<ID>, S extends BaseService<T, ID>, ID extends Serializable>
        implements BaseController<T, ID> {

    protected final S service;
    protected final Class<T> entityClass;

    public CrudBaseController(S service, Class<T> entityClass) {
        this.service = service;
        this.entityClass = entityClass;
    }

    @Override
    public ResponseEntity<ResponseDTO<T>> create(@Valid @RequestBody T entity) {
        T savedEntity = service.save(entity);
        return new ResponseEntity<>(
                ResponseDTO.success(savedEntity, entityClass.getSimpleName() + " created successfully"),
                HttpStatus.CREATED
        );
    }

    @Override
    public ResponseEntity<ResponseDTO<T>> findById(@PathVariable ID id) {
        return service.findById(id)
                .map(entity -> ResponseEntity.ok(ResponseDTO.success(entity)))
                .orElseThrow(() -> new ResourceNotFoundException(entityClass.getSimpleName() + " not found with id: " + id));
    }

    @Override
    public ResponseEntity<ResponseDTO<T>> findByUuid(@PathVariable String uuid) {
        return service.findByUuid(uuid)
                .map(entity -> ResponseEntity.ok(ResponseDTO.success(entity)))
                .orElseThrow(() -> new ResourceNotFoundException(entityClass.getSimpleName() + " not found with uuid: " + uuid));
    }

    @Override
    public ResponseEntity<ResponseDTO<List<T>>> findAll() {
        List<T> entities = service.findAll();
        return ResponseEntity.ok(ResponseDTO.success(entities));
    }

    @Override
    public ResponseEntity<ResponseDTO<Page<T>>> findAll(Pageable pageable) {
        Page<T> page = service.findAll(pageable);
        return ResponseEntity.ok(ResponseDTO.success(page));
    }

    @Override
    public ResponseEntity<ResponseDTO<T>> update(@PathVariable ID id, @Valid @RequestBody T entity) {
        // Ensure the entity exists
        if (!service.existsById(id)) {
            throw new ResourceNotFoundException(entityClass.getSimpleName() + " not found with id: " + id);
        }

        // Set the ID if it's not already set
        if (entity.getId() == null) {
            entity.setId(id);
        }

        T updatedEntity = service.save(entity);
        return ResponseEntity.ok(
                ResponseDTO.success(updatedEntity, entityClass.getSimpleName() + " updated successfully")
        );
    }

    @Override
    public ResponseEntity<ResponseDTO<Void>> deleteById(@PathVariable ID id) {
        // Ensure the entity exists
        if (!service.existsById(id)) {
            throw new ResourceNotFoundException(entityClass.getSimpleName() + " not found with id: " + id);
        }

        service.deleteById(id);
        return ResponseEntity.ok(
                ResponseDTO.success(null, entityClass.getSimpleName() + " deleted successfully")
        );
    }
}```

## BaseDTO.java

```java
package io.joshuasalcedo.commonlibs.domain.base.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base DTO class for transferring entity data to and from clients.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseDTO implements Serializable {

    private String uuid;
    @Builder.Default
    private boolean active = true;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    private Long version;
}```

## ResponseDTO.java

```java
package io.joshuasalcedo.commonlibs.domain.base.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Generic response wrapper for API responses.
 *
 * @param <T> the type of data in the response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO<T> {

    private T data;
    private boolean success;
    private String message;
    private LocalDateTime timestamp;

    /**
     * Create a successful response with data.
     *
     * @param data the data to include in the response
     * @param message the success message
     * @param <T> the type of data
     * @return a ResponseDTO object
     */
    public static <T> ResponseDTO<T> success(T data, String message) {
        return ResponseDTO.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create a successful response with data and a default message.
     *
     * @param data the data to include in the response
     * @param <T> the type of data
     * @return a ResponseDTO object
     */
    public static <T> ResponseDTO<T> success(T data) {
        return success(data, "Operation completed successfully");
    }

    /**
     * Create an error response.
     *
     * @param message the error message
     * @param <T> the type of data (will be null)
     * @return a ResponseDTO object
     */
    public static <T> ResponseDTO<T> error(String message) {
        return ResponseDTO.<T>builder()
                .success(false)
                .data(null)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}```

## BaseService.java

```java
package io.joshuasalcedo.commonlibs.domain.base.service;

import io.joshuasalcedo.commonlibs.domain.base.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Base service interface for CRUD operations on entities.
 *
 * @param <T> the entity type
 * @param <ID> the type of the entity's primary key
 */
public interface BaseService<T extends BaseEntity<ID>, ID extends Serializable> {

    /**
     * Save a new entity or update an existing one.
     *
     * @param entity the entity to save
     * @return the saved entity
     */
    T save(T entity);

    /**
     * Find an entity by its ID.
     *
     * @param id the ID to search for
     * @return an Optional containing the entity if found, or empty if not found
     */
    Optional<T> findById(ID id);

    /**
     * Find an entity by its UUID.
     *
     * @param uuid the UUID to search for
     * @return an Optional containing the entity if found, or empty if not found
     */
    Optional<T> findByUuid(String uuid);

    /**
     * Find all entities.
     *
     * @return a list of all entities
     */
    List<T> findAll();

    /**
     * Find all entities with pagination.
     *
     * @param pageable pagination information
     * @return a page of entities
     */
    Page<T> findAll(Pageable pageable);

    /**
     * Delete an entity by its ID.
     *
     * @param id the ID of the entity to delete
     */
    void deleteById(ID id);

    /**
     * Check if an entity exists by its ID.
     *
     * @param id the ID to check
     * @return true if an entity with the given ID exists, false otherwise
     */
    boolean existsById(ID id);
}```

## CrudBaseService.java

```java
package io.joshuasalcedo.commonlibs.domain.base.service;

import io.joshuasalcedo.commonlibs.domain.base.BaseEntity;
import io.joshuasalcedo.commonlibs.domain.base.BaseRepository;
import io.joshuasalcedo.commonlibs.domain.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Base implementation of BaseService that provides standard CRUD operations.
 *
 * @param <T> the entity type
 * @param <R> the repository type
 * @param <ID> the type of the entity's primary key
 */
@Validated
@Transactional
public class CrudBaseService<T extends BaseEntity<ID>, R extends BaseRepository<T, ID>, ID extends Serializable>
        implements BaseService<T, ID> {

    protected final R repository;

    public CrudBaseService(R repository) {
        this.repository = repository;
    }

    @Override
    public T save(@Valid T entity) {
        return repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<T> findByUuid(String uuid) {
        return Optional.ofNullable(repository.findByUuid(uuid));
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public void deleteById(ID id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(ID id) {
        return repository.existsById(id);
    }

    /**
     * Utility method to get an entity by ID or throw an exception if not found.
     *
     * @param id the ID of the entity to find
     * @return the entity if found
     * @throws ResourceNotFoundException if the entity is not found
     */
    protected T getEntityById(ID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entity not found with id: " + id));
    }
}```

## BaseEntityObserver.java

```java
package io.joshuasalcedo.commonlibs.domain.listeners;

import io.joshuasalcedo.commonlibs.domain.base.BaseEntity;
import io.joshuasalcedo.commonlibs.domain.logging.LoggingManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Base implementation of EntityLifecycleObserver that provides common functionality
 * for entity lifecycle event observation.
 * 
 * This class simplifies creating custom entity observers by providing:
 * - Type-safe event handling methods for each lifecycle event
 * - Automatic type checking and casting
 * - Built-in error handling
 * - Configurable entity class filtering
 */
public abstract class BaseEntityObserver implements EntityLifecycleObserver {

    private final Set<Class<?>> observedEntityClasses;

    /**
     * Create a base observer for the specified entity classes.
     * 
     * @param entityClasses the entity classes to observe
     */
    protected BaseEntityObserver(Class<?>... entityClasses) {
        this.observedEntityClasses = new HashSet<>(Arrays.asList(entityClasses));
    }

    /**
     * Check if this observer is interested in events for the given entity class.
     * 
     * @param entityClass The class of the entity
     * @return true if this observer is configured to observe the entity class, false otherwise
     */
    @Override
    public boolean isInterestedIn(Class<?> entityClass) {
        return observedEntityClasses.isEmpty() || observedEntityClasses.contains(entityClass);
    }

    /**
     * Handle an entity lifecycle event. This method delegates to the appropriate
     * event-specific method based on the event type.
     * 
     * @param event The lifecycle event
     * @param entity The entity that triggered the event
     */
    @Override
    public void onEntityEvent(EntityLifecycleEvent event, Object entity) {
        try {
            // Ensure the entity is a BaseEntity
            if (!(entity instanceof BaseEntity)) {
                // Skip non-BaseEntity objects
                return;
            }

            BaseEntity<?> baseEntity = (BaseEntity<?>) entity;
            
            // Delegate to the appropriate event handler
            switch (event) {
                case PRE_PERSIST:
                    onPrePersist(baseEntity);
                    break;
                case POST_PERSIST:
                    onPostPersist(baseEntity);
                    break;
                case PRE_UPDATE:
                    onPreUpdate(baseEntity);
                    break;
                case POST_UPDATE:
                    onPostUpdate(baseEntity);
                    break;
                case PRE_REMOVE:
                    onPreRemove(baseEntity);
                    break;
                case POST_REMOVE:
                    onPostRemove(baseEntity);
                    break;
                case POST_LOAD:
                    onPostLoad(baseEntity);
                    break;
                default:
                    // Do nothing for unknown events
            }
        }    catch (Exception e) {
            // Get a logger from LoggingManager to avoid circular dependencies
            LoggingManager.getLogger(BaseEntityObserver.class).error("Error in entity lifecycle event handling", e);
        }

    }

    /**
     * Called before an entity is persisted.
     * Override this method to handle pre-persist events.
     * 
     * @param entity the entity that will be persisted
     */
    protected void onPrePersist(BaseEntity<?> entity) {
        // Default implementation does nothing
    }

    /**
     * Called after an entity is persisted.
     * Override this method to handle post-persist events.
     * 
     * @param entity the persisted entity
     */
    protected void onPostPersist(BaseEntity<?> entity) {
        // Default implementation does nothing
    }

    /**
     * Called before an entity is updated.
     * Override this method to handle pre-update events.
     * 
     * @param entity the entity that will be updated
     */
    protected void onPreUpdate(BaseEntity<?> entity) {
        // Default implementation does nothing
    }

    /**
     * Called after an entity is updated.
     * Override this method to handle post-update events.
     * 
     * @param entity the updated entity
     */
    protected void onPostUpdate(BaseEntity<?> entity) {
        // Default implementation does nothing
    }

    /**
     * Called before an entity is removed.
     * Override this method to handle pre-remove events.
     * 
     * @param entity the entity that will be removed
     */
    protected void onPreRemove(BaseEntity<?> entity) {
        // Default implementation does nothing
    }

    /**
     * Called after an entity is removed.
     * Override this method to handle post-remove events.
     * 
     * @param entity the removed entity
     */
    protected void onPostRemove(BaseEntity<?> entity) {
        // Default implementation does nothing
    }

    /**
     * Called after an entity is loaded from the database.
     * Override this method to handle post-load events.
     * 
     * @param entity the loaded entity
     */
    protected void onPostLoad(BaseEntity<?> entity) {
        // Default implementation does nothing
    }
}```

## EntityLifecycleEvent.java

```java
package io.joshuasalcedo.commonlibs.domain.listeners;

/**
 * Types of entity lifecycle events.
 */
public enum EntityLifecycleEvent {
    PRE_PERSIST,
    POST_PERSIST,
    PRE_UPDATE,
    POST_UPDATE,
    PRE_REMOVE,
    POST_REMOVE,
    POST_LOAD
}```

## EntityLifecycleListener.java

```java
package io.joshuasalcedo.commonlibs.domain.listeners;

import jakarta.persistence.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Entity lifecycle listener that notifies registered observers of entity changes.
 */
public class EntityLifecycleListener {

    private static final List<EntityLifecycleObserver> observers = new CopyOnWriteArrayList<>();
    
    public static void registerObserver(EntityLifecycleObserver observer) {
        observers.add(observer);
    }
    
    public static void removeObserver(EntityLifecycleObserver observer) {
        observers.remove(observer);
    }
    
    @PrePersist
    public void prePersist(Object entity) {
        notifyObservers(EntityLifecycleEvent.PRE_PERSIST, entity);
    }
    
    @PostPersist
    public void postPersist(Object entity) {
        notifyObservers(EntityLifecycleEvent.POST_PERSIST, entity);
    }
    
    @PreUpdate
    public void preUpdate(Object entity) {
        notifyObservers(EntityLifecycleEvent.PRE_UPDATE, entity);
    }
    
    @PostUpdate
    public void postUpdate(Object entity) {
        notifyObservers(EntityLifecycleEvent.POST_UPDATE, entity);
    }
    
    @PreRemove
    public void preRemove(Object entity) {
        notifyObservers(EntityLifecycleEvent.PRE_REMOVE, entity);
    }
    
    @PostRemove
    public void postRemove(Object entity) {
        notifyObservers(EntityLifecycleEvent.POST_REMOVE, entity);
    }
    
    @PostLoad
    public void postLoad(Object entity) {
        notifyObservers(EntityLifecycleEvent.POST_LOAD, entity);
    }
    
    private void notifyObservers(EntityLifecycleEvent event, Object entity) {
        for (EntityLifecycleObserver observer : observers) {
            if (observer.isInterestedIn(entity.getClass())) {
                observer.onEntityEvent(event, entity);
            }
        }
    }
}```

## EntityLifecycleObserver.java

```java
package io.joshuasalcedo.commonlibs.domain.listeners;

/**
 * Interface for observers of entity lifecycle events.
 */
public interface EntityLifecycleObserver {
    
    /**
     * Check if this observer is interested in events for the given entity class.
     *
     * @param entityClass The class of the entity
     * @return true if interested, false otherwise
     */
    boolean isInterestedIn(Class<?> entityClass);
    
    /**
     * Handle an entity lifecycle event.
     *
     * @param event The lifecycle event
     * @param entity The entity that triggered the event
     */
    void onEntityEvent(EntityLifecycleEvent event, Object entity);
}```

## EntityLifecycleObserverRegistrar.java

```java
package io.joshuasalcedo.commonlibs.domain.listeners;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EntityLifecycleObserverRegistrar implements BeanPostProcessor , DisposableBean {
    // Keep track of registered observers
    private final List<EntityLifecycleObserver> registeredObservers = new ArrayList<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof EntityLifecycleObserver) {
            EntityLifecycleListener.registerObserver((EntityLifecycleObserver) bean);
        }
        return bean;
    }

    @Override
    public void destroy() {
        // Clean up when the Spring context is closed
        registeredObservers.forEach(EntityLifecycleListener::removeObserver);
        registeredObservers.clear();
    }
}```

## EntityLifecycleLogger.java

```java
package io.joshuasalcedo.commonlibs.domain.logging;

import io.joshuasalcedo.commonlibs.domain.base.BaseEntity;
import io.joshuasalcedo.commonlibs.domain.listeners.BaseEntityObserver;
import io.joshuasalcedo.commonlibs.domain.listeners.EntityLifecycleEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Entity lifecycle observer that logs entity lifecycle events.
 */
@Component
@ConditionalOnProperty(prefix = "io.joshuasalcedo.common.logging", name = "entity-lifecycle-logging", havingValue = "true", matchIfMissing = true)
public class EntityLifecycleLogger extends BaseEntityObserver {
    
    private final LoggingService logger;
    
    public EntityLifecycleLogger(LoggingFactory loggingFactory) {
        // This observer is interested in all entity types
        super();
        this.logger = loggingFactory.getLogger(EntityLifecycleLogger.class);
    }
    
    @Override
    protected void onPrePersist(BaseEntity<?> entity) {
        if (logger.isDebugEnabled()) {
            logger.debug("Entity PRE_PERSIST: {} (UUID: {})", 
                entity.getClass().getSimpleName(), entity.getUuid());
        }
    }
    
    @Override
    protected void onPostPersist(BaseEntity<?> entity) {
        if (logger.isDebugEnabled()) {
            logger.debug("Entity POST_PERSIST: {} (UUID: {}, ID: {})", 
                entity.getClass().getSimpleName(), entity.getUuid(), entity.getId());
        }
    }
    
    @Override
    protected void onPreUpdate(BaseEntity<?> entity) {
        if (logger.isDebugEnabled()) {
            logger.debug("Entity PRE_UPDATE: {} (UUID: {}, ID: {})", 
                entity.getClass().getSimpleName(), entity.getUuid(), entity.getId());
        }
    }
    
    @Override
    protected void onPostUpdate(BaseEntity<?> entity) {
        if (logger.isDebugEnabled()) {
            logger.debug("Entity POST_UPDATE: {} (UUID: {}, ID: {})", 
                entity.getClass().getSimpleName(), entity.getUuid(), entity.getId());
        }
    }
    
    @Override
    protected void onPreRemove(BaseEntity<?> entity) {
        if (logger.isDebugEnabled()) {
            logger.debug("Entity PRE_REMOVE: {} (UUID: {}, ID: {})", 
                entity.getClass().getSimpleName(), entity.getUuid(), entity.getId());
        }
    }
    
    @Override
    protected void onPostRemove(BaseEntity<?> entity) {
        if (logger.isDebugEnabled()) {
            logger.debug("Entity POST_REMOVE: {} (UUID: {}, ID: {})", 
                entity.getClass().getSimpleName(), entity.getUuid(), entity.getId());
        }
    }
    
    @Override
    protected void onPostLoad(BaseEntity<?> entity) {
        if (logger.isTraceEnabled()) {
            logger.trace("Entity POST_LOAD: {} (UUID: {}, ID: {})", 
                entity.getClass().getSimpleName(), entity.getUuid(), entity.getId());
        }
    }
}```

## LogLevel.java

```java
package io.joshuasalcedo.commonlibs.domain.logging;

/**
 * Enum representing available log levels.
 */
public enum LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR
}```

## Logged.java

```java
package io.joshuasalcedo.commonlibs.domain.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that should be logged.
 * This annotation can be used on any method to enable detailed logging
 * of method entry, exit, parameters, return values, and execution time.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Logged {
    /**
     * The log level to use.
     */
    LogLevel level() default LogLevel.DEBUG;

    /**
     * Whether to log method parameters.
     */
    boolean logParameters() default true;

    /**
     * Whether to log the return value.
     */
    boolean logReturnValue() default false;

    /**
     * Whether to log execution time.
     */
    boolean logExecutionTime() default true;
}```

## LoggingAspect.java

```java
package io.joshuasalcedo.commonlibs.domain.logging;

import io.joshuasalcedo.commonlibs.properties.LoggingProperties;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.StopWatch;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * AOP aspect for method-level logging.
 */
@Aspect
public class LoggingAspect {
    private final LoggingFactory loggingFactory;
    private final LoggingProperties properties;

    public LoggingAspect(LoggingFactory loggingFactory, LoggingProperties properties) {
        this.loggingFactory = loggingFactory;
        this.properties = properties;
    }

    /**
     * Pointcut for all methods in the base package or subpackages.
     */
    @Pointcut("within(io.joshuasalcedo..*)")
    public void applicationPackagePointcut() {
        // Method is empty as this is just a pointcut definition
    }

    /**
     * Pointcut for methods annotated with @Logged annotation.
     */
    @Pointcut("@annotation(io.joshuasalcedo.commonlibs.domain.logging.Logged)")
    public void loggedAnnotationPointcut() {
        // Method is empty as this is just a pointcut definition
    }

    /**
     * Pointcut for all entity lifecycle event methods.
     */
    @Pointcut("execution(* io.joshuasalcedo.commonlibs.domain.listeners.EntityLifecycleListener.*(..))")
    public void entityLifecyclePointcut() {
        // Method is empty as this is just a pointcut definition
    }

    /**
     * Log method execution time and parameters for methods annotated with @Logged.
     */
    @Around("loggedAnnotationPointcut()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecutionInternal(joinPoint, true);
    }

    /**
     * Log entity lifecycle events if enabled.
     */
    @Around("entityLifecyclePointcut()")
    public Object logEntityLifecycle(ProceedingJoinPoint joinPoint) throws Throwable {
        // Check if entity lifecycle logging is enabled
        if (!properties.isEntityLifecycleLogging()) {
            return joinPoint.proceed();
        }
        return logMethodExecutionInternal(joinPoint, false);
    }

    /**
     * Internal method to log method execution details.
     */
    private Object logMethodExecutionInternal(ProceedingJoinPoint joinPoint, boolean detailed) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        LoggingService logger = loggingFactory.getLogger(joinPoint.getTarget().getClass());

        // Get method annotation for custom settings if present
        Logged annotation = method.getAnnotation(Logged.class);
        LogLevel level = annotation != null ? annotation.level() : LogLevel.DEBUG;

        StopWatch stopWatch = new StopWatch();
        try {
            if (detailed && logger.isDebugEnabled()) {
                String params = getMethodParams(joinPoint);
                log(logger, level, "Executing: {}#{} with params: [{}]",
                        joinPoint.getTarget().getClass().getSimpleName(),
                        method.getName(), params);
            }

            stopWatch.start();
            Object result = joinPoint.proceed();
            stopWatch.stop();

            if (detailed && logger.isDebugEnabled()) {
                log(logger, level, "Completed: {}#{} in {} ms",
                        joinPoint.getTarget().getClass().getSimpleName(),
                        method.getName(),
                        stopWatch.getTotalTimeMillis());
            }

            return result;
        } catch (Throwable e) {
            stopWatch.stop();

            log(logger, LogLevel.ERROR, "Exception in {}#{} after {} ms: {}",
                    joinPoint.getTarget().getClass().getSimpleName(),
                    method.getName(),
                    stopWatch.getTotalTimeMillis(),
                    e.getMessage());

            throw e;
        }
    }

    /**
     * Log exceptions thrown by methods in the application package.
     */
    @AfterThrowing(pointcut = "applicationPackagePointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        LoggingService logger = loggingFactory.getLogger(joinPoint.getTarget().getClass());

        if (logger.isErrorEnabled()) {
            logger.error(
                    "Exception in {}#{}: {}",
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    e.getMessage(),
                    e
            );
        }
    }

    /**
     * Format method parameters for logging.
     */
    private String getMethodParams(JoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getArgs())
                .map(arg -> arg != null ? arg.toString() : "null")
                .collect(Collectors.joining(", "));
    }

    /**
     * Log a message with the appropriate log level.
     */
    private void log(LoggingService logger, LogLevel level, String message, Object... args) {
        switch (level) {
            case TRACE:
                logger.trace(message, args);
                break;
            case DEBUG:
                logger.debug(message, args);
                break;
            case INFO:
                logger.info(message, args);
                break;
            case WARN:
                logger.warn(message, args);
                break;
            case ERROR:
                logger.error(message, args);
                break;
        }
    }
}```

## LoggingConfigurator.java

```java
package io.joshuasalcedo.commonlibs.domain.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;

import io.joshuasalcedo.commonlibs.properties.LoggingProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Configures Logback programmatically based on properties.
 * This is useful for adding file appenders or customizing logging behavior.
 */
@Component
@ConditionalOnProperty(prefix = "io.joshuasalcedo.common.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LoggingConfigurator {
    private final LoggingProperties properties;
    LoggingService logger;

    @Value("${logging.file.path:${user.home}/.app/logs}")
    private String logFilePath;

    @Value("${spring.application.name:application}")
    private String applicationName;


    public LoggingConfigurator(LoggingProperties properties, LoggingService logger) {
        this.properties = properties;
        this.logger = logger;
    }

    @PostConstruct
    public void configureLogback() {
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

            // Configure root logger level
            Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.toLevel(properties.getLevel(), Level.INFO));

            // Configure colorful console appender
            configureConsoleAppender(loggerContext, rootLogger);

            // Configure file appender
            configureFileAppender(loggerContext, rootLogger);

            // Configure base package logger
            if (properties.getBasePackage() != null && !properties.getBasePackage().isEmpty()) {
                Logger basePackageLogger = loggerContext.getLogger(properties.getBasePackage());
                basePackageLogger.setLevel(Level.toLevel(properties.getLevel(), Level.INFO));
            }

            logger.info("Logging configured with level: {}, base package: {}, log file: {}/{}.log",
                    properties.getLevel(), properties.getBasePackage(), logFilePath, applicationName);

        } catch (Exception e) {
            System.err.println("Error configuring logging: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configure a colorful console appender.
     */
    private void configureConsoleAppender(LoggerContext context, Logger rootLogger) {
        // Create the encoder for the appender
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{HH:mm:ss.SSS} %highlight(%-5level) %magenta([%thread]) %boldCyan(%-40.40logger{39}) : %msg%n");
        encoder.setCharset(StandardCharsets.UTF_8);
        encoder.start();

        // Create the console appender
        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setContext(context);
        appender.setName("CONSOLE");
        appender.setEncoder(encoder);
        appender.start();

        // Add the appender to the root logger
        rootLogger.addAppender(appender);
    }

    /**
     * Configure a rolling file appender for log files.
     */
    private void configureFileAppender(LoggerContext context, Logger rootLogger) {
        // Ensure log files exists
        File logDir = new File(logFilePath);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        // Create the encoder for the appender
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %-40.40logger{39} : %msg%n");
        encoder.setCharset(StandardCharsets.UTF_8);
        encoder.start();

        // Create the rolling file appender
        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setContext(context);
        appender.setName("FILE");
        appender.setFile(logFilePath + "/" + applicationName + ".log");
        appender.setEncoder(encoder);
        appender.setAppend(true);

        // Create and set the rolling policy
        SizeAndTimeBasedRollingPolicy<ILoggingEvent> policy = new SizeAndTimeBasedRollingPolicy<>();
        policy.setContext(context);
        policy.setParent(appender);
        policy.setFileNamePattern(logFilePath + "/" + applicationName + "-%d{yyyy-MM-dd}-%i.log.gz");
        policy.setMaxFileSize(FileSize.valueOf("10MB"));
        policy.setMaxHistory(30);
        policy.setTotalSizeCap(FileSize.valueOf("3GB"));
        policy.start();

        appender.setRollingPolicy(policy);
        appender.start();

        // Add the appender to the root logger
        rootLogger.addAppender(appender);
    }
}```

## LoggingFactory.java

```java
package io.joshuasalcedo.commonlibs.domain.logging;

import io.joshuasalcedo.commonlibs.properties.LoggingProperties;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoggingFactory {
    private final LoggingProperties properties;
    private final Map<String, LoggingService> loggerCache = new ConcurrentHashMap<>();

    public LoggingFactory(LoggingProperties properties) {
        this.properties = properties;
    }

    public LoggingService getLogger(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Logger class cannot be null");
        }
        return loggerCache.computeIfAbsent(
                clazz.getName(),
                name -> new LoggingService(clazz, properties)
        );
    }

    public LoggingService getLogger(String name) {
        return loggerCache.computeIfAbsent(
                name,
                n -> new LoggingService(name, properties)
        );
    }

    public void clearCache() {
        loggerCache.clear();
    }
}```

## LoggingManager.java

```java
package io.joshuasalcedo.commonlibs.domain.logging;

/**
 * Static utility class for accessing loggers throughout the application.
 * This provides a convenient way to get a logger without directly 
 * accessing the Spring context.
 */
public final class LoggingManager {
    private static LoggingFactory loggingFactory;
    


    public static void setLoggingFactory(LoggingFactory factory) {
        loggingFactory = factory;
    }

    public static LoggingService getLogger(Class<?> clazz) {
        if (loggingFactory == null) {
            throw new IllegalStateException("LoggingFactory not initialized");
        }
        return loggingFactory.getLogger(clazz);
    }

    public static LoggingService getLogger(String name) {
        if (loggingFactory == null) {
            throw new IllegalStateException("LoggingFactory not initialized");
        }
        return loggingFactory.getLogger(name);
    }

}```

## LoggingService.java

```java
package io.joshuasalcedo.commonlibs.domain.logging;

import io.joshuasalcedo.commonlibs.properties.LoggingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Core logging service that provides consistent logging functionality.
 */

public class LoggingService {
    private final LoggingProperties properties;
    private final Logger logger;

    // Patterns for sensitive data masking
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b");
    private static final Pattern CREDIT_CARD_PATTERN =
            Pattern.compile("\\b(?:\\d[ -]*?){13,16}\\b");
    private static final Pattern SSN_PATTERN =
            Pattern.compile("\\b\\d{3}[-]?\\d{2}[-]?\\d{4}\\b");

    public LoggingService(Class<?> loggerClass, LoggingProperties properties) {
        this.properties = properties;
        this.logger = LoggerFactory.getLogger(loggerClass);
    }

    public LoggingService(String loggerName, LoggingProperties properties) {
        this.properties = properties;
        this.logger = LoggerFactory.getLogger(loggerName);
    }



    /**
     * Log a trace message.
     */
    public void trace(String message, Object... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(maskSensitiveData(message), args);
        }
    }

    /**
     * Log a debug message.
     */
    public void debug(String message, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(maskSensitiveData(message), args);
        }
    }

    /**
     * Log an info message.
     */
    public void info(String message, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(maskSensitiveData(message), args);
        }
    }

    /**
     * Log a warning message.
     */
    public void warn(String message, Object... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(maskSensitiveData(message), args);
        }
    }

    /**
     * Log a warning message with exception.
     */
    public void warn(String message, Throwable throwable) {
        if (logger.isWarnEnabled()) {
            logger.warn(maskSensitiveData(message), throwable);
        }
    }

    /**
     * Log an error message.
     */
    public void error(String message, Object... args) {
        if (logger.isErrorEnabled()) {
            logger.error(maskSensitiveData(message), args);
        }
    }

    /**
     * Log an error message with exception.
     */
    public void error(String message, Throwable throwable) {
        if (logger.isErrorEnabled()) {
            logger.error(maskSensitiveData(message), throwable);
        }
    }

    /**
     * Log an error with an exception and additional context.
     */
    public void error(String message, Throwable throwable, Object... context) {
        if (logger.isErrorEnabled()) {
            String contextStr = StringUtils.hasText(message) ? message + " " : "";
            contextStr += "Context: " + Arrays.toString(context);
            logger.error(maskSensitiveData(contextStr), throwable);
        }
    }

    /**
     * Mask sensitive data in the log message if enabled.
     */
    private String maskSensitiveData(String message) {
        if (!properties.isMaskSensitiveData() || message == null) {
            return message;
        }

        // Mask email addresses
        message = EMAIL_PATTERN.matcher(message).replaceAll(match -> {
            String email = match.group();
            int atIndex = email.indexOf('@');
            if (atIndex > 1) {
                return email.charAt(0) + "****" + email.substring(atIndex);
            }
            return email;
        });

        // Mask credit card numbers
        message = CREDIT_CARD_PATTERN.matcher(message).replaceAll(match -> {
            String cc = match.group().replaceAll("[ -]", "");
            if (cc.length() >= 13) {
                return "****" + cc.substring(cc.length() - 4);
            }
            return cc;
        });

        // Mask SSNs
        message = SSN_PATTERN.matcher(message).replaceAll("***-**-****");

        return message;
    }

    /**
     * Check if the trace level is enabled.
     */
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    /**
     * Check if the debug level is enabled.
     */
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    /**
     * Check if the info level is enabled.
     */
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    /**
     * Check if the warn level is enabled.
     */
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    /**
     * Check if the error level is enabled.
     */
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }
}```

## SQLQueryLogger.java

```java
package io.joshuasalcedo.commonlibs.domain.logging;


import io.joshuasalcedo.commonlibs.properties.LoggingProperties;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SQL query logger that logs database queries when enabled.
 * Uses datasource-proxy to intercept and log SQL queries.
 */
@Component
@ConditionalOnClass(name = "net.ttddyy.dsproxy.listener.QueryExecutionListener")
@ConditionalOnProperty(prefix = "io.joshuasalcedo.logging", name = "sql-logging", havingValue = "true")
public class SQLQueryLogger implements QueryExecutionListener {

    private final LoggingService logger;
    private final boolean enabled;

    public SQLQueryLogger(LoggingService logger, LoggingProperties properties) {
        this.logger = logger;
        this.enabled = properties.isSqlLogging();
    }

    @Override
    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        // No action needed before query execution
    }

    @Override
    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        if (!enabled || !logger.isDebugEnabled()) {
            return;
        }

        // Log each query with its execution time
        for (QueryInfo queryInfo : queryInfoList) {
            String query = queryInfo.getQuery();
            
            // Format and log the query
            logger.debug("SQL Query executed in {} ms:\n{}",
                         execInfo.getElapsedTime(),
                         formatQuery(query));
            
            // Log parameters if present
            logger.debug("Query parameters: {}", queryInfo.getParametersList().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", ")));
        }
    }
    
    /**
     * Format the SQL query for better readability in logs.
     */
    private String formatQuery(String query) {
        // Simple SQL formatting - in a real implementation, 
        // you might use a SQL formatter library
        return query.replaceAll("(?i)\\s+(FROM|WHERE|JOIN|AND|OR|GROUP BY|ORDER BY|HAVING)\\s+", 
                               "\n$1 ");
    }
}```

## CustomBanner.java

```java
package io.joshuasalcedo.commonlibs.implementation;

import io.joshuasalcedo.commonlibs.properties.BannerProperties;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class CustomBanner implements Banner {

    private final BannerProperties properties;

    public CustomBanner(BannerProperties properties) {
        this.properties = properties;
    }

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        try {
            String bannerPath = properties.getLocation().replace("classpath:", "");
            Resource resource = new ClassPathResource(bannerPath);

            if (!resource.exists()) {
                // Fallback to default banner if custom one doesn't exist
                resource = new ClassPathResource("banner.txt");
            }

            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                String banner = FileCopyUtils.copyToString(reader);

                // Replace standard Spring Boot placeholders
                String appName = environment.getProperty("spring.application.name", "Application");
                String appVersion = environment.getProperty("spring.application.version", "Unknown Version");

                // Replace both standard Spring Boot placeholders and our custom ones
                banner = banner.replace("${application.title}", appName);
                banner = banner.replace("${application.name}", appName);
                banner = banner.replace("${application.version}", appVersion);
                banner = banner.replace("${spring-boot.formatted-version}", " (v" + SpringBootVersion.getVersion() + ")");

                // Apply color if specified
                if (!"default".equals(properties.getColor())) {
                    String color = properties.getColor().toUpperCase();
                    banner = "${AnsiColor." + color + "}" + banner + "${AnsiColor.DEFAULT}";
                }

                out.println(banner);
            }
        } catch (IOException e) {
            // If error occurs, just continue without banner
            out.println("Error loading banner: " + e.getMessage());
        }
    }
}```

## AuditingEntityListener.java

```java
package io.joshuasalcedo.commonlibs.listeners;


import io.joshuasalcedo.commonlibs.domain.base.BaseEntity;
import io.joshuasalcedo.commonlibs.properties.DomainProperties;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Entity listener that handles auditing fields automatically.
 */
@Data
@Component
public class AuditingEntityListener {

    private DomainProperties.EntityConfig entityProperties;

    // Default constructor required for JPA
    public AuditingEntityListener() {
    }

    @PrePersist
    public void setCreationAuditFields(Object entity) {
        if (entity instanceof BaseEntity) {
            BaseEntity<?> baseEntity =
                    (BaseEntity<?>) entity;

            if (baseEntity.getCreatedAt() == null) {
                baseEntity.setCreatedAt(LocalDateTime.now());
            }

            if (baseEntity.getCreatedBy() == null) {
                baseEntity.setCreatedBy(getCurrentAuditor());
            }

            baseEntity.setUpdatedAt(baseEntity.getCreatedAt());
            baseEntity.setUpdatedBy(baseEntity.getCreatedBy());
        }
    }

    @PreUpdate
    public void setUpdateAuditFields(Object entity) {
        if (entity instanceof BaseEntity) {
            BaseEntity<?> baseEntity =
                    (BaseEntity<?>) entity;

            baseEntity.setUpdatedAt(LocalDateTime.now());
            baseEntity.setUpdatedBy(getCurrentAuditor());
        }
    }

    private String getCurrentAuditor() {
        if (entityProperties == null) {
            return "system";
        }
        return entityProperties.getSystemUsername();
    }
}```

## BannerProperties.java

```java
package io.joshuasalcedo.commonlibs.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "io.joshuasalcedo.banner")
public class BannerProperties {
    // Getters and setters
    /**
     * Enable or disable the custom banner
     */
    private boolean enabled = true;

    /**
     * Path to the custom banner resource
     */
    private String location = "classpath:banner.txt";

    /**
     * Text color of the banner
     */
    private String color = "default";

}```

## DomainProperties.java

```java
package io.joshuasalcedo.commonlibs.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "io.joshuasalcedo.common.domain")
public class DomainProperties {
    /**
     * Base entity configuration
     */
    private EntityConfig entity = new EntityConfig();

    /**
     * Repository configuration
     */
    private RepositoryConfig repository = new RepositoryConfig();

    /**
     * Service configuration
     */
    private ServiceConfig service = new ServiceConfig();

    /**
     * Controller configuration
     */
    private ControllerConfig controller = new ControllerConfig();

    // Nested configuration classes
    public static class EntityConfig {


        /**
         * Enable or disable entity lifecycle events.
         */
        private boolean lifecycleEventsEnabled = true;

        /**
         * Enable or disable automatic entity auditing.
         */
        private boolean enableAuditing = true;

        /**
         * Default name to use for system actions when no user is authenticated.
         */
        private String systemUsername = "system";





        /**
         * Enable or disable soft delete functionality.
         */
        private boolean enableSoftDelete = true;

        // Getters and setters
        public boolean isEnableAuditing() {
            return enableAuditing;
        }

        public void setEnableAuditing(boolean enableAuditing) {
            this.enableAuditing = enableAuditing;
        }

        public String getSystemUsername() {
            return systemUsername;
        }

        public void setSystemUsername(String systemUsername) {
            this.systemUsername = systemUsername;
        }

        public boolean isEnableSoftDelete() {
            return enableSoftDelete;
        }

        public void setEnableSoftDelete(boolean enableSoftDelete) {
            this.enableSoftDelete = enableSoftDelete;
        }

        public boolean isLifecycleEventsEnabled() {
            return lifecycleEventsEnabled;
        }

        public void setLifecycleEventsEnabled(boolean lifecycleEventsEnabled) {
            this.lifecycleEventsEnabled = lifecycleEventsEnabled;
        }
    }

    public static class RepositoryConfig {
        /**
         * Enable or disable custom repository functionality.
         */
        private boolean enabled = true;

        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class ServiceConfig {
        /**
         * Enable or disable service layer.
         */
        private boolean enabled = true;

        /**
         * Enable validation at service layer.
         */
        private boolean enableValidation = true;

        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnableValidation() {
            return enableValidation;
        }

        public void setEnableValidation(boolean enableValidation) {
            this.enableValidation = enableValidation;
        }
    }

    public static class ControllerConfig {
        /**
         * Enable or disable generic controllers.
         */
        private boolean enabled = true;

        /**
         * Base path for REST endpoints.
         */
        private String basePath = "/api";

        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }
    }

    // Getters and setters for the main config
    public EntityConfig getEntity() {
        return entity;
    }

    public void setEntity(EntityConfig entity) {
        this.entity = entity;
    }

    public RepositoryConfig getRepository() {
        return repository;
    }

    public void setRepository(RepositoryConfig repository) {
        this.repository = repository;
    }

    public ServiceConfig getService() {
        return service;
    }

    public void setService(ServiceConfig service) {
        this.service = service;
    }

    public ControllerConfig getController() {
        return controller;
    }

    public void setController(ControllerConfig controller) {
        this.controller = controller;
    }
}```

## LoggingProperties.java

```java
package io.joshuasalcedo.commonlibs.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the logging system.
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "io.joshuasalcedo.common.logging")
public class LoggingProperties {
    /**
     * Enable or disable the custom logging
     */
    private boolean enabled = true;

    /**
     * Logging level for the application
     * Valid values: TRACE, DEBUG, INFO, WARN, ERROR
     */
    private String level = "INFO";

    /**
     * Enable or disable logging for entity lifecycle events
     */
    private boolean entityLifecycleLogging = true;

    /**
     * Enable or disable HTTP request logging
     */
    private boolean requestLogging = true;

    /**
     * Enable or disable log masking for sensitive data
     */
    private boolean maskSensitiveData = true;

    /**
     * Enable or disable SQL query logging
     */
    private boolean sqlLogging = false;

    /**
     * Pattern to use for logging
     */
    private String pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n";
    
    /**
     * Package name to apply logging to (defaults to your base package)
     */
    private String basePackage = "io.joshuasalcedo";
}```

## application.properties

```properties
# Application Configuration
spring.application.name=common-library
spring.application.version=@project.version@

# Banner Configuration
io.joshuasalcedo.banner.enabled=true
io.joshuasalcedo.banner.color=

spring.output.ansi.enabled=ALWAYS

logging.file.path=${user.home}/.app/logs/

# Configure the logging system
io.joshuasalcedo.common.logging.enabled=true
io.joshuasalcedo.common.logging.level=INFO
io.joshuasalcedo.common.logging.entity-lifecycle-logging=true
io.joshuasalcedo.common.logging.mask-sensitive-data=true
io.joshuasalcedo.common.logging.sql-logging=false

# Set log levels for specific packages
logging.level.io.joshuasalcedo=DEBUG
logging.level.org.springframework=INFO
logging.level.org.hibernate=INFO

# io.joshuasalcedo.banner.location=classpath:custom-banner.txt```

## CommonLibraryApplicationTest.java

```java
package io.joshuasalcedo.commonlibs;

import io.joshuasalcedo.commonlibs.domain.logging.LoggingFactory;
import io.joshuasalcedo.commonlibs.domain.logging.LoggingManager;
import io.joshuasalcedo.commonlibs.properties.LoggingProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest

class CommonLibraryApplicationTest {


    @Test
    void contextLoads() {
    }
}```

## application.properties

```properties
# Application Configuration
spring.application.name=common-library
spring.application.version=@project.version@

# Banner Configuration
io.joshuasalcedo.banner.enabled=true
io.joshuasalcedo.banner.color=

spring.output.ansi.enabled=ALWAYS

logging.file.path=${user.home}/.app/logs/

# Configure the logging system
io.joshuasalcedo.common.logging.enabled=true
io.joshuasalcedo.common.logging.level=INFO
io.joshuasalcedo.common.logging.entity-lifecycle-logging=true
io.joshuasalcedo.common.logging.mask-sensitive-data=true
io.joshuasalcedo.common.logging.sql-logging=false

# Set log levels for specific packages
logging.level.io.joshuasalcedo=DEBUG
logging.level.org.springframework=INFO
logging.level.org.hibernate=INFO

# io.joshuasalcedo.banner.location=classpath:custom-banner.txt```

