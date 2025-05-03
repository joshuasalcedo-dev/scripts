package io.joshuasalcedo.script.model;

import io.joshuasalcedo.commonlibs.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

/**
 * Entity class for storing scripts in the ScriptManager system.
 * This entity represents executable scripts with metadata.
 */
@Entity
@Table(name = "scripts", indexes = {
        @Index(name = "idx_script_title", columnList = "title"),
        @Index(name = "idx_script_language", columnList = "language")
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@RequiredArgsConstructor
public class Script extends BaseEntity<Long> {

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

    @Version
    @Column(name = "optlock", columnDefinition = "integer DEFAULT 0", nullable = false)
    private long version;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;



    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Script script = (Script) o;
        return getId() != null && Objects.equals(getId(), script.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
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