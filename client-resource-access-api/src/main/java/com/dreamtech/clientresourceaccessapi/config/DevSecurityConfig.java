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
                // Disabling Spring Security's CSRF protection for local developing
                .csrf(csrf -> csrf.disable())

                // Bypass Security checking for local developing
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .formLogin(form -> form.disable())     // Completely disables the login page UI
                .httpBasic(basic -> basic.disable());   // Disables the basic browser auth popup too

        return http.build();
    }

}