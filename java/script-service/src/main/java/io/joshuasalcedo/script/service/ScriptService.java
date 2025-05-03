package io.joshuasalcedo.script.service;

import io.joshuasalcedo.commonlibs.domain.base.service.BaseService;
import io.joshuasalcedo.commonlibs.domain.base.service.CrudBaseService;
import io.joshuasalcedo.script.model.Script;
import io.joshuasalcedo.script.repository.ScriptRepository;
import org.springframework.stereotype.Service;


@Service
public class ScriptService extends CrudBaseService<Script, ScriptRepository, Long> {

    public ScriptService (ScriptRepository repository) {
        super(repository);
    }
}
