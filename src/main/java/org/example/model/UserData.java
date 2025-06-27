package org.example.model;

import lombok.Getter;
import lombok.Setter;
import org.example.pojo.Role;

@Getter
@Setter
public class UserData {
    private Integer id;
    private String email;
    private Role role;
}
