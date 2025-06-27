package org.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:application.properties")
public class SecurityConfig {
    @Autowired
    private Environment env;

    public boolean isSupervisor(String email) {
        String supervisorEmail = env.getProperty("supervisor.email", "").toLowerCase().trim();
        return email != null && email.toLowerCase().trim().equals(supervisorEmail);
    }

}

