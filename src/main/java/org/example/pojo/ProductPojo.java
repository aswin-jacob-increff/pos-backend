package org.example.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table (name = "product")
public class ProductPojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Integer id;

    @Column(unique = true)
    private String barcode;

    @Column(nullable = false)
    private String clientName;

    @Column(unique = true)
    private String name;

    private Double mrp;
    private String imageUrl;

}