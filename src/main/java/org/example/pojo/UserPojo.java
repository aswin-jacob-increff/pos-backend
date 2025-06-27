package org.example.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class UserPojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Integer id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // Stored as BCrypt hash

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}

