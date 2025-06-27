package org.example.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Setter
@Getter
public class OrderData {

    private Integer id;
    private Instant date;

}
