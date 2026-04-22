package org.example.planner_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class PlannerBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlannerBackendApplication.class, args);
    }

}

