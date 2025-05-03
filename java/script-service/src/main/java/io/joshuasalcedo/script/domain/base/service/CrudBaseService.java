package io.joshuasalcedo.script.domain.base.service;

import io.joshuasalcedo.script.domain.base.BaseEntity;
import io.joshuasalcedo.script.domain.base.BaseRepository;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Base implementation of the BaseService interface that provides CRUD operations.
 * @param <T> The entity type
 * @param <R> The repository type
 * @param <ID> The type of the entity's ID
 */
@Transactional
public class CrudBaseService<T extends BaseEntity<ID>, R extends BaseRepository<T, ID>, ID extends Serializable> 
        implements BaseService<T, ID> {

    protected final R repository;

    public CrudBaseService(R repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }

    @Override
    public T save(T entity) {
        return repository.save(entity);
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

    @Override
    public BaseRepository<T, ID> getRepository() {
        return repository;
    }
}