package io.joshuasalcedo.script.domain.base;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * Base repository interface that extends JpaRepository to provide common CRUD operations.
 * @param <T> The entity type
 * @param <ID> The type of the entity's ID
 */
@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity<ID>, ID extends Serializable> extends JpaRepository<T, ID> {
}