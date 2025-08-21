package books.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.experimental.SuperBuilder;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@NoArgsConstructor
@MappedSuperclass
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public sealed abstract class BaseEntity permits Book, Author {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @CreationTimestamp
    @Column(updatable = false, nullable = true)
    private Timestamp created;

    @UpdateTimestamp
    @Column(updatable = true, nullable = true)
    private Timestamp updated;

}
