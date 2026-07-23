package com.dreamtech.clientresourceaccessapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test") // Activates application-test.yml
public class SecurityConfigTest {

    @Autowired
    private Environment environment;

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(SecurityConfig.class);

    @Test
    void testActiveProfile() {
        String[] env = environment.getActiveProfiles();
        assertThat(Arrays.toString(env)).isEqualTo("[test]");
    }

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

}
