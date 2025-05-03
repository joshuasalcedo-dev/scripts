package io.joshuasalcedo.script.domain.base.service;

import io.joshuasalcedo.script.domain.base.BaseEntity;
import io.joshuasalcedo.script.domain.base.BaseRepository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Base service interface that defines common operations for entities.
 * @param <T> The entity type
 * @param <ID> The type of the entity's ID
 */
public interface BaseService<T extends BaseEntity<ID>, ID extends Serializable> {

    /**
     * Finds all entities.
     * @return a list of all entities
     */
    List<T> findAll();

    /**
     * Finds an entity by its ID.
     * @param id the ID of the entity to find
     * @return an Optional containing the found entity, or empty if not found
     */
    Optional<T> findById(ID id);

    /**
     * Saves an entity.
     * @param entity the entity to save
     * @return the saved entity
     */
    T save(T entity);

    /**
     * Deletes an entity by its ID.
     * @param id the ID of the entity to delete
     */
    void deleteById(ID id);

    /**
     * Checks if an entity with the given ID exists.
     * @param id the ID to check
     * @return true if an entity with the given ID exists, false otherwise
     */
    boolean existsById(ID id);

    /**
     * Gets the repository used by this service.
     * @return the repository
     */
    BaseRepository<T, ID> getRepository();
}