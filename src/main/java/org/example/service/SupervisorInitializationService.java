package org.example.service;

import org.example.dto.UserDto;
import org.example.flow.UserFlow;
import org.example.pojo.UserPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to automatically initialize supervisor account from application.properties
 * This ensures the supervisor account always exists and is never ignored
 */
@Component
public class SupervisorInitializationService implements CommandLineRunner {

    @Autowired
    private UserFlow userFlow;

    @Value("${supervisor.email:admin@example.com}")
    private String supervisorEmail;

    @Value("${supervisor.password:admin123}")
    private String supervisorPassword;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeSupervisorAccount();
    }

    private void initializeSupervisorAccount() {
        try {
            // Check if supervisor account already exists
            UserPojo existingSupervisor = userFlow.getByEmail(supervisorEmail);
            
            if (existingSupervisor == null) {
                // Create supervisor account
                UserPojo supervisorPojo = new UserPojo();
                supervisorPojo.setEmail(supervisorEmail.toLowerCase().trim());
                supervisorPojo.setPassword(supervisorPassword.trim());
                supervisorPojo.setRole(org.example.enums.Role.SUPERVISOR);
                
                userFlow.signup(supervisorPojo);
                System.out.println("✅ Supervisor account initialized: " + supervisorEmail);
            } else {
                // Update supervisor password if it changed in properties
                if (!userFlow.checkPassword(supervisorPassword, existingSupervisor.getPassword())) {
                    existingSupervisor.setPassword(supervisorPassword.trim());
                    userFlow.update(existingSupervisor.getId(), existingSupervisor);
                    System.out.println("✅ Supervisor password updated: " + supervisorEmail);
                } else {
                    System.out.println("✅ Supervisor account already exists: " + supervisorEmail);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize supervisor account: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 