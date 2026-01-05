package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan("org.example")
@SpringBootApplication
public class ReviewManageApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReviewManageApplication.class, args);
    }
}