package io.joshuasalcedo.script.model;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity class for storing scripts in the ScriptManager system.
 * This entity represents executable scripts with metadata.
 */
@Entity
@Table(name = "scripts")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Script {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "script_seq")
    @SequenceGenerator(name = "script_seq", sequenceName = "script_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false, length = 50)
    private ScriptLanguage language;

    @Column(name = "help", nullable = true, length = 2000)
    private String help;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;


    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    @ElementCollection
    private Map<String, String> logs = new HashMap<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Script script = (Script) o;
        return Objects.equals(id, script.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Enable the script
     */
    public void enable() {
        this.isEnabled = true;
    }

    /**
     * Disable the script
     */
    public void disable() {
        this.isEnabled = false;
    }
}
