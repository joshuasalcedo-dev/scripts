package io.joshuasalcedo.script.domain.base.controller;

import io.joshuasalcedo.script.domain.base.BaseEntity;
import io.joshuasalcedo.script.domain.base.service.BaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
 * Base controller that provides CRUD endpoints for entities.
 * @param <T> The entity type
 * @param <S> The service type
 * @param <ID> The type of the entity's ID
 */
public class CrudBaseController<T extends BaseEntity<ID>, S extends BaseService<T, ID>, ID extends Serializable> {

    protected final S service;
    protected final Class<T> entityClass;

    public CrudBaseController(S service, Class<T> entityClass) {
        this.service = service;
        this.entityClass = entityClass;
    }

    /**
     * Gets all entities.
     * @return a list of all entities
     */
    @GetMapping
    public ResponseEntity<List<T>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * Gets an entity by its ID.
     * @param id the ID of the entity to get
     * @return the entity if found, or a 404 response if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<T> getById(@PathVariable ID id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new entity.
     * @param entity the entity to create
     * @return the created entity with a 201 status code
     */
    @PostMapping
    public ResponseEntity<T> create(@RequestBody T entity) {
        T savedEntity = service.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEntity);
    }

    /**
     * Updates an existing entity.
     * @param id the ID of the entity to update
     * @param entity the updated entity data
     * @return the updated entity if found, or a 404 response if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<T> update(@PathVariable ID id, @RequestBody T entity) {
        return service.findById(id)
                .map(existingEntity -> {
                    entity.setId(id);
                    return ResponseEntity.ok(service.save(entity));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes an entity by its ID.
     * @param id the ID of the entity to delete
     * @return a 204 response if successful, or a 404 response if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable ID id) {
        if (service.existsById(id)) {
            service.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}