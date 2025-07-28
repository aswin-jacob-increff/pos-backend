package org.example.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.util.TimeUtil;

@Setter
@Getter
@Entity
@Table (name = "product")
public class ProductPojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "product_id_generator")
    @TableGenerator(
        name = "product_id_generator",
        table = "id_generators",
        pkColumnName = "gen_name",
        valueColumnName = "gen_val",
        pkColumnValue = "product_id",
        allocationSize = 1
    )
    private Integer id;

    @Column(unique = true)
    private String barcode;

    @Column(nullable = false)
    private Integer clientId;

    @Column(unique = false)
    private String name;

    private Double mrp;
    private String imageUrl;

    public void setMrp(Double mrp) {
        this.mrp = mrp == null ? null : TimeUtil.round2(mrp);
    }
    public Double getMrp() {
        return mrp == null ? null : TimeUtil.round2(mrp);
    }
}