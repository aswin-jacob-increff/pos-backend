package org.example.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.ZonedDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class AbstractPojo {

    @Version
    private int version;

    @CreationTimestamp
    @Column(updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    private ZonedDateTime updatedAt;
}
