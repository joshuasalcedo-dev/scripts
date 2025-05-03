package io.joshuasalcedo.script.repository;

import io.joshuasalcedo.commonlibs.domain.base.BaseRepository;
import io.joshuasalcedo.script.model.Script;
import org.springframework.stereotype.Repository;

@Repository
public interface ScriptRepository extends BaseRepository<Script, Long> {
}
