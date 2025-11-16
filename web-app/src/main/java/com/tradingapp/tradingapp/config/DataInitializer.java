package com.tradingapp.tradingapp.config;

import com.tradingapp.tradingapp.entities.User;
import com.tradingapp.tradingapp.entities.UserRole;
import com.tradingapp.tradingapp.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {

            long adminCount = userRepository.findAll()
                    .stream()
                    .filter(u -> u.getRole() == UserRole.ADMIN)
                    .count();

            if (adminCount == 0) {

                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123")) // паролата
                        .role(UserRole.ADMIN)
                        .build();

                userRepository.save(admin);

                System.out.println("=========================================");
                System.out.println(" ADMIN USER WAS CREATED AUTOMATICALLY ");
                System.out.println(" Username: admin");
                System.out.println(" Password: admin123");
                System.out.println("=========================================");
            }
        };
    }
}
