package io.joshuasalcedo.script.controller;

import io.joshuasalcedo.commonlibs.domain.base.controller.CrudBaseController;
import io.joshuasalcedo.script.model.Script;
import io.joshuasalcedo.script.service.ScriptService;
import org.springframework.stereotype.Controller;

@Controller
public class ServiceController extends CrudBaseController<Script, ScriptService, Long> {

    public ServiceController (ScriptService service, Class<Script> entityClass) {
        super(service, entityClass);
    }
}
