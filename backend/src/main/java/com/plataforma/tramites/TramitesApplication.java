package com.plataforma.tramites;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.plataforma.tramites.modules")
public class TramitesApplication {

    public static void main(String[] args) {
        SpringApplication.run(TramitesApplication.class, args);
    }
}
