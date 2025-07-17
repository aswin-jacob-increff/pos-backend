package org.example.model.data;

import lombok.Getter;
import lombok.Setter;
import org.example.model.enums.Role;

@Getter
@Setter
public class UserData {
    private Integer id;
    private String email;
    private Role role;
}
