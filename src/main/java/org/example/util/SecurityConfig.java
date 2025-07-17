package org.example.util;

import org.example.model.constants.Supervisors;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    public boolean isSupervisor(String email) {
        String supervisorEmail = Supervisors.ADMIN;
        return email != null && email.toLowerCase().trim().equals(supervisorEmail);
    }

}