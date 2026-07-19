package com.dreamtech.clientresourceaccessapi.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("qatest")
public class QaTestSecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Allows Postman to send POST requests
                .csrf(csrf -> csrf.disable())

                //Disabling Spring Security's CSRF protection is safe here - This will be applied to QA local only.
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

}