package org.example.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table (name = "client")
public class ClientPojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "client_id_generator")
    @TableGenerator(
        name = "client_id_generator",
        table = "id_generators",
        pkColumnName = "gen_name",
        valueColumnName = "gen_val",
        pkColumnValue = "client_id",
        allocationSize = 1
    )
    private Integer id;

    @Column(unique = true)
    private String clientName;

    @Column(nullable = false)
    private Boolean status = true; // Default to true (active)
}

