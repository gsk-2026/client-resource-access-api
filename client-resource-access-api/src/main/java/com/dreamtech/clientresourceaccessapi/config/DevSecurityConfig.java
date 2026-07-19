package com.dreamtech.clientresourceaccessapi.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("dev") // This class ONLY loads when you run with the 'dev' profile active
public class DevSecurityConfig {
    // java -Dspring.profiles.active=dev -jar target/client-resource-access-api-0.0.1-SNAPSHOT.jar

    @Bean
    public SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
        http
                // Allows Postman to send POST requests
                .csrf(csrf -> csrf.disable())

                //Disabling Spring Security's CSRF protection is safe here - This will be applied to DEV local only.
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

}