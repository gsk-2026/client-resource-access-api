package com.dreamtech.clientresourceaccessapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// This tells Spring to ignore missing database properties for now
//@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@SpringBootApplication
//@EnableJpaRepositories(basePackages = "com.dreamtech.clientresourceaccessapi.repository")
public class ClientResourceAccessApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientResourceAccessApiApplication.class, args);
    }

}
