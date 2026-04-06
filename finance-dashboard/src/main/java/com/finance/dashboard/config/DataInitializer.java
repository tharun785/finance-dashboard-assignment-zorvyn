package com.finance.dashboard.config;

import com.finance.dashboard.model.ERole;
import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.Role;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.repository.RoleRepository;
import com.finance.dashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FinancialRecordRepository recordRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Initializing Database ===");

        // Create roles if they don't exist
        if (roleRepository.count() == 0) {
            System.out.println("Creating roles...");
            roleRepository.save(new Role(ERole.ROLE_ADMIN));
            roleRepository.save(new Role(ERole.ROLE_ANALYST));
            roleRepository.save(new Role(ERole.ROLE_VIEWER));
            System.out.println("Roles created successfully!");
        }

        // Create admin user
        User admin = null;
        if (!userRepository.existsByUsername("admin")) {
            System.out.println("Creating admin user...");
            admin = new User("admin", "admin@finance.com", passwordEncoder.encode("admin123"));
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(roleRepository.findByName(ERole.ROLE_ADMIN).get());
            admin.setRoles(adminRoles);
            admin.setActive(true);
            admin = userRepository.save(admin);
            System.out.println("Admin user created!");
        } else {
            admin = userRepository.findByUsername("admin").get();
        }

        // Create analyst user
        if (!userRepository.existsByUsername("analyst")) {
            System.out.println("Creating analyst user...");
            User analyst = new User("analyst", "analyst@finance.com", passwordEncoder.encode("analyst123"));
            Set<Role> analystRoles = new HashSet<>();
            analystRoles.add(roleRepository.findByName(ERole.ROLE_ANALYST).get());
            analyst.setRoles(analystRoles);
            analyst.setActive(true);
            userRepository.save(analyst);
            System.out.println("Analyst user created!");
        }

        // Create viewer user
        if (!userRepository.existsByUsername("viewer")) {
            System.out.println("Creating viewer user...");
            User viewer = new User("viewer", "viewer@finance.com", passwordEncoder.encode("viewer123"));
            Set<Role> viewerRoles = new HashSet<>();
            viewerRoles.add(roleRepository.findByName(ERole.ROLE_VIEWER).get());
            viewer.setRoles(viewerRoles);
            viewer.setActive(true);
            userRepository.save(viewer);
            System.out.println("Viewer user created!");
        }

        // Create sample records (global - not associated with any specific user)
        if (recordRepository.count() == 0) {
            System.out.println("Creating sample records...");

            FinancialRecord record1 = new FinancialRecord();
            record1.setAmount(50000.0);
            record1.setType("INCOME");
            record1.setCategory("Salary");
            record1.setDate(LocalDate.now());
            record1.setDescription("Monthly salary");
            recordRepository.save(record1);

            FinancialRecord record2 = new FinancialRecord();
            record2.setAmount(15000.0);
            record2.setType("EXPENSE");
            record2.setCategory("Rent");
            record2.setDate(LocalDate.now());
            record2.setDescription("Monthly rent");
            recordRepository.save(record2);

            FinancialRecord record3 = new FinancialRecord();
            record3.setAmount(5000.0);
            record3.setType("EXPENSE");
            record3.setCategory("Food");
            record3.setDate(LocalDate.now());
            record3.setDescription("Groceries");
            recordRepository.save(record3);

            FinancialRecord record4 = new FinancialRecord();
            record4.setAmount(2000.0);
            record4.setType("EXPENSE");
            record4.setCategory("Transport");
            record4.setDate(LocalDate.now());
            record4.setDescription("Fuel");
            recordRepository.save(record4);

            System.out.println("Sample records created!");
        }

        System.out.println("=== Database Initialization Complete ===");
    }
}