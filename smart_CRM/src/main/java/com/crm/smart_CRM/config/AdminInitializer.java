package com.crm.smart_CRM.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.crm.smart_CRM.Enum.UserRole;
import com.crm.smart_CRM.model.User;
import com.crm.smart_CRM.repository.UserRepository;

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

   

    @Override
    public void run(String... args) {

        String adminEmail = "sunilofficial781@gmail.com";

        if (!userRepository.existsByEmail(adminEmail)) {

            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail(adminEmail);
            admin.setPassword("Admin@123");
            admin.setRole(UserRole.ADMIN);
//            admin.setActive(true);

            userRepository.save(admin);

            System.out.println("✅ Admin user created");
        }
    }
}

