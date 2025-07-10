package org.example.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "order_item")
public class OrderItemPojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "order_item_id_generator")
    @TableGenerator(
        name = "order_item_id_generator",
        table = "id_generators",
        pkColumnName = "gen_name",
        valueColumnName = "gen_val",
        pkColumnValue = "order_item_id",
        allocationSize = 1
    )
    private Integer id;

    @Column(nullable = false)
    private Integer orderId;

    @Column(nullable = false)
    private String productBarcode;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String clientName;

    private Double productMrp;
    private String productImageUrl;

    private Integer quantity;
    private Double sellingPrice;
    private Double amount;

    public void setSellingPrice(Double sellingPrice) {
        this.sellingPrice = sellingPrice == null ? null : org.example.util.TimeUtil.round2(sellingPrice);
    }
    public Double getSellingPrice() {
        return sellingPrice == null ? null : org.example.util.TimeUtil.round2(sellingPrice);
    }
    public void setAmount(Double amount) {
        this.amount = amount == null ? null : org.example.util.TimeUtil.round2(amount);
    }
    public Double getAmount() {
        return amount == null ? null : org.example.util.TimeUtil.round2(amount);
    }
    public void setProductMrp(Double productMrp) {
        this.productMrp = productMrp == null ? null : org.example.util.TimeUtil.round2(productMrp);
    }
    public Double getProductMrp() {
        return productMrp == null ? null : org.example.util.TimeUtil.round2(productMrp);
    }
}