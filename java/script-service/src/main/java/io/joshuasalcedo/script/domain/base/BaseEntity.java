package io.joshuasalcedo.script.domain.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base entity class that provides common fields and functionality for all entities.
 * @param <ID> The type of the entity's ID
 */
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@ToString
public abstract class BaseEntity<ID extends Serializable> implements Serializable {

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Gets the ID of the entity.
     * @return the ID
     */
    public abstract ID getId();

    /**
     * Sets the ID of the entity.
     * @param id the ID to set
     */
    public abstract void setId(ID id);

    /**
     * Method called before persisting the entity to set the creation timestamp.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Method called before updating the entity to set the update timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}