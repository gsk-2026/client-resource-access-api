package com.dreamtech.clientresourceaccessapi.config;
/*
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
*/
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
/*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
*/
import org.springframework.security.web.SecurityFilterChain;

//import static org.assertj.core.api.Assertions.assertThat;

@Configuration
public class SecurityConfigTest {
    /*
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(SecurityConfig.class);
    */

    @Bean
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disables CSRF block
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // Allows all endpoints
        return http.build();
    }

    /*
    @Test
    void shouldRegisterPasswordEncoderBeanAsBCrypt() {
        contextRunner.run(context -> {
            // Assert bean presence and type
            assertThat(context).hasSingleBean(PasswordEncoder.class);
            PasswordEncoder encoder = context.getBean(PasswordEncoder.class);
            assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);

            // Behavioral check
            String rawPassword = "mySecretPassword";
            String encoded = encoder.encode(rawPassword);
            assertThat(encoder.matches(rawPassword, encoded)).isTrue();
        });
    }

    @Test
    void shouldRegisterOpenApiBeanWithCorrectMetadata() {
        contextRunner.run(context -> {
            // Assert bean presence
            assertThat(context).hasSingleBean(OpenAPI.class);
            OpenAPI openAPI = context.getBean(OpenAPI.class);

            // Assert metadata values
            assertThat(openAPI.getInfo()).isNotNull();
            assertThat(openAPI.getInfo().getTitle()).isEqualTo("Client Resource Access");
            assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
            assertThat(openAPI.getInfo().getDescription()).isEqualTo("API documentation for managing resource access paths.");
        });
    }
    */
}
