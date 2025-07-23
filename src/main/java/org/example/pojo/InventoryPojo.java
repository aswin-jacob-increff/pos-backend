package org.example.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table (name = "inventory")
public class InventoryPojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "inventory_id_generator")
    @TableGenerator(
        name = "inventory_id_generator",
        table = "id_generators",
        pkColumnName = "gen_name",
        valueColumnName = "gen_val",
        pkColumnValue = "inventory_id",
        allocationSize = 1
    )
    private Integer id;

    @Column(nullable = false)
    private Integer productId;

    private Integer quantity;
}