package org.example.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "orders")
public class OrderPojo extends AbstractPojo {

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Integer id;

    private Instant date;

    public void setDateTime(Instant date) {
        this.date = date;
    }
}