package io.joshuasalcedo.script.service;

import io.joshuasalcedo.script.model.Script;
import io.joshuasalcedo.script.repository.ScriptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CrudService {

    private final ScriptRepository repository;

    @Autowired
    public CrudService (ScriptRepository repository) {
        this.repository = repository;
    }

    public List<Script> findAll() {
        return repository.findAll();
    }

    public Optional<Script> findById(Long id) {
        return repository.findById(id);
    }

    public Script save(Script script) {
        return repository.save(script);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
