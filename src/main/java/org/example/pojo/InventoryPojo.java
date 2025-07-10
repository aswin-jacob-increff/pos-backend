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

    @Column(unique = true)
    private String productBarcode;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String clientName;

    private Double productMrp;
    private String productImageUrl;

    private Integer quantity;

    public void setProductMrp(Double productMrp) {
        this.productMrp = productMrp == null ? null : org.example.util.TimeUtil.round2(productMrp);
    }
    public Double getProductMrp() {
        return productMrp == null ? null : org.example.util.TimeUtil.round2(productMrp);
    }
}