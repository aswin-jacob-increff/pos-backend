package org.example.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.enums.Role;

@Getter
@Setter
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class UserPojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "user_id_generator")
    @TableGenerator(
        name = "user_id_generator",
        table = "id_generators",
        pkColumnName = "gen_name",
        valueColumnName = "gen_val",
        pkColumnValue = "user_id",
        allocationSize = 1
    )
    private Integer id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // Stored as BCrypt hash

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}

