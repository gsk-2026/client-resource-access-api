package com.dreamtech.clientresourceaccessapi;

import com.dreamtech.clientresourceaccessapi.dto.*;
import com.dreamtech.clientresourceaccessapi.model.Client;
import com.dreamtech.clientresourceaccessapi.model.Resource;
import com.dreamtech.clientresourceaccessapi.repository.ClientRepository;
import com.dreamtech.clientresourceaccessapi.repository.ClientResourceAccessRepository;
import com.dreamtech.clientresourceaccessapi.repository.ResourceRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@Testcontainers
@ActiveProfiles("docker-test") // Loads application-docker-test.yml
@SpringBootTest(
        classes = ClientResourceAccessApiApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class ClientResourceAccessApiDockerE2E {
    /*  End-To-End Test:
        The Dockerized (not real raw DIT/SIT/UAT) Oracle Database is applied for the E2E Testing.
        Note: More positive/normal and negative/edge E2E scenarios can be implemented later.   */

    @Autowired
    private Environment environment;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ClientResourceAccessRepository clientResourceAccessRepository;

    @LocalServerPort
    private Integer port;
    private TestRestTemplate restTemplate;

    // Define a static container but DO NOT use the @Container annotation.
    // This allows us to start it conditionally.
    static OracleContainer oracleContainer;

    @BeforeAll
    static void setupAll() {
        // Only spin up Testcontainers if USE_LOCAL_DOCKER=true is provided
        //if ("true".equalsIgnoreCase(System.getenv("USE_LOCAL_DOCKER"))) {
            oracleContainer = new OracleContainer(DockerImageName.parse(
                    "gvenzl/oracle-free:23-slim-faststart"))
                    .withUsername("e2e_username")         // this is dummy-username used by container to create Username
                    .withPassword("secure_e2e_password"); // this is dummy-password used by container to create Password
            oracleContainer.start();
        //}
    }

    // Dynamically feed the container's random connection properties to Spring Boot
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Double-check initialization state before registering dynamic overrides
        if (oracleContainer != null && oracleContainer.isRunning()) {
            // Overrides application-docker-test.yml dynamically for local Testcontainers execution
            registry.add("spring.datasource.url", oracleContainer::getJdbcUrl);
            registry.add("spring.datasource.username", oracleContainer::getUsername);
            registry.add("spring.datasource.password", oracleContainer::getPassword);

            // Force the driver just in case the container exposes an alternate scheme
            registry.add("spring.datasource.driver-class-name", () -> "oracle.jdbc.driver.OracleDriver");
        }
        /*  If oracleContainer is null, Spring naturally falls back to the env vars mapped in application-docker-test.yml
            in this case, the yml needs to provide datasource url, username, password, et al.  */
    }


    @BeforeEach
    void setUpEach() {
        RestTemplateBuilder builder = new RestTemplateBuilder().baseUri("http://localhost:" + port);
        restTemplate = new TestRestTemplate(builder);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        clientResourceAccessRepository.deleteAll();
        resourceRepository.deleteAll();
        clientRepository.deleteAll();
    }


    @Test
    void testActiveProfile() {
        // This will print the active profiles (e.g., [test]) helping you match the YAML suffix
        String[] env = environment.getActiveProfiles();
        assertThat(Arrays.toString(env)).isEqualTo("[docker-test]");
    }


    @Nested
    class PostAction {
        @Test
        void createClient() {
            String clientKey = "Client-Key";
            String clientSecret = "VeryStrongSecretPassword";
            String clientDescription = "The Client Description";

            ClientPostRequest request = ClientPostRequest.builder()
                    .key(clientKey)
                    .secret(clientSecret)
                    .description(clientDescription)
                    .build();

            given()
                    .contentType("application/json")
                    .body(request)
                    .when()
                    .post("/api/v1/client")
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("id", notNullValue())
                    .body("key", equalTo(request.key()))
                    .body("description", equalTo(request.description()));
        }

        @Test
        void createResource() {
            String resourceKey = "Resource-Key";
            String resourceType = "Resource-Type";
            String resourceDescription = "Resource Description";

            ResourcePostRequest request = ResourcePostRequest.builder()
                    .key(resourceKey)
                    .type(resourceType)
                    .description(resourceDescription)
                    .build();

            given()
                    .contentType("application/json")
                    .body(request)
                    .when()
                    .post("/api/v1/resource")
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("id", notNullValue())
                    .body("key", equalTo(request.key()))
                    .body("type", equalTo(request.type()))
                    .body("description", equalTo(request.description()));
        }
    }


    @Nested
    class PatchAction {
        @Test
        void updateClient() {
            Client client = populateClient("Client-Key-A",
                    "VeryStrongSecrectPassword-A",
                    "Client-Description-A");

            ClientPatchRequest request = ClientPatchRequest.builder()
                    .key(client.getKey())
                    .description("UPDATED" + client.getDescription())
                    .build();

            given()
                    .contentType("application/json")
                    .pathParam("id", client.getId())
                    .body(request)
                    .when()
                    .patch("/api/v1/client/{id}")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("id", equalTo(client.getId().intValue()))
                    .body("key", equalTo(request.key()))
                    .body("description", equalTo(request.description()));
        }

        @Test
        void updateResource() {
            Resource resource = populateResource("Resource-Key-A",
                    "Resource-Type-A",
                    "Resource-Description-A");

            ResourcePatchRequest request = ResourcePatchRequest.builder()
                    .key(resource.getKey())
                    .type("Will-Be_Updated-To-" + resource.getType())
                    .description(resource.getDescription() + "-To-Be-Updated")
                    .build();

            given()
                    .contentType("application/json")
                    .pathParam("id", resource.getId())
                    .body(request)
                    .when()
                    .patch("/api/v1/resource/{id}")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("id", equalTo(resource.getId().intValue()))
                    .body("key", equalTo(request.key()))
                    .body("type", equalTo(request.type()))
                    .body("description", equalTo(request.description()));
        }
    }


    @Nested
    class PutAction {
        @Test
        void replaceClient_existing() {
            Client client = populateClient("Existing-Client-Key-B",
                    "Existing-VeryStrongSecrectPassword-B",
                    "Existing-Client-Description-B");

            ClientPutRequest request = ClientPutRequest.builder()
                    .key(client.getKey() + "-Replaced")
                    .description("REPLACED-" + client.getDescription())
                    .build();

            given()
                    .contentType("application/json")
                    .pathParam("id", client.getId())
                    .body(request)
                    .when()
                    .put("/api/v1/client/{id}")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("id", equalTo(client.getId().intValue()))
                    .body("key", equalTo(request.key()))
                    .body("description", equalTo(request.description()));
        }

        @Test
        void replaceClient_new() {
            ClientPutRequest request = ClientPutRequest.builder()
                    .key("New-Client-Key-C-Replaced")
                    .description("New-REPLACED-Client-Descriprion-C")
                    .build();

            given()
                    .contentType("application/json")
                    .pathParam("id", 123456789L)
                    .body(request)
                    .when()
                    .put("/api/v1/client/{id}")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("id", notNullValue())
                    .body("key", equalTo(request.key()))
                    .body("description", equalTo(request.description()));
        }

        @Test
        void replaceResource_existing() {
            Resource resource = populateResource("Existing-Resource-Key-B",
                    "Existing-Resource-Type-B",
                    "Existing-Resource-Description-B");

            ResourcePutRequest request = ResourcePutRequest.builder()
                    .key(resource.getKey())
                    .type("Will-Be_Replaced-To-" + resource.getType())
                    .description(resource.getDescription() + "-To-Be-Replaced")
                    .build();

            given()
                    .contentType("application/json")
                    .pathParam("id", resource.getId())
                    .body(request)
                    .when()
                    .put("/api/v1/resource/{id}")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("id", equalTo(resource.getId().intValue()))
                    .body("key", equalTo(request.key()))
                    .body("type", equalTo(request.type()))
                    .body("description", equalTo(request.description()));
        }

        @Test
        void replaceResource_new() {
            ResourcePutRequest request = ResourcePutRequest.builder()
                    .key("New-Resource-Key-C-Replaced")
                    .type("New-Will-Be_Replaced-To-Resource-Type-C")
                    .description("New-Resource-Description-C-To-Be-Replaced")
                    .build();

            given()
                    .contentType("application/json")
                    .pathParam("id", 123456789L)
                    .body(request)
                    .when()
                    .put("/api/v1/resource/{id}")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("id", notNullValue())
                    .body("key", equalTo(request.key()))
                    .body("type", equalTo(request.type()))
                    .body("description", equalTo(request.description()));
        }
    }


    @Nested
    class DeleteAction {
        @Test
        void deleteClientById() {
            Client client = populateClient("Client-Key-D",
                    "VeryStrongSecrectPassword-D",
                    "Client-Description-D");

            given()
                    .pathParam("id", client.getId())
                    .when()
                    .delete("/api/v1/client/{id}")
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        void deleteResourceById() {
            Resource resource = populateResource("Resource-Key-D",
                    "Resource-Type-D",
                    "Resource-Description-D");

            given()
                    .pathParam("id", resource.getId())
                    .when()
                    .delete("/api/v1/resource/{id}")
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }
    }


    @Nested
    class GetAction {
        @Test
        void getClientById() {
            Client client = populateClient("Client-Key-E",
                    "VeryStrongSecrectPassword-E",
                    "Client-Description-E");

            given()
                    .pathParam("id", client.getId())
                    .when()
                    .get("/api/v1/client/{id}")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("id", equalTo(client.getId().intValue()))
                    .body("key", equalTo(client.getKey()))
                    .body("description", equalTo(client.getDescription()));
        }

        @Test
        void getClientKeyById() {
            Client client = populateClient("Client-Key-F",
                    "VeryStrongSecrectPassword-F",
                    "Client-Description-F");

            given()
                    .pathParam("id", client.getId())
                    .when()
                    .get("/api/v1/client/{id}/key")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(is(client.getKey()));
        }

        @Test
        void getClientDescriptionById() {
            Client client = populateClient("Client-Key-F",
                    "VeryStrongSecrectPassword-F",
                    "Client-Description-F");

            given()
                    .pathParam("id", client.getId())
                    .when()
                    .get("/api/v1/client/{id}/description")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(is(client.getDescription()));
        }

        @Test
        void getResourceById() {
            Resource resource = populateResource("Resource-Key-E",
                    "Resource-Type-E",
                    "Resource-Description-E");

            given()
                    .pathParam("id", resource.getId())
                    .when()
                    .get("/api/v1/resource/{id}")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("id", equalTo(resource.getId().intValue()))
                    .body("key", equalTo(resource.getKey()))
                    .body("type", equalTo(resource.getType()))
                    .body("description", equalTo(resource.getDescription()));
        }

        @Test
        void getResourceKeyById() {
            Resource resource = populateResource("Resource-Key-F",
                    "Resource-Type-F",
                    "Resource-Description-F");

            given()
                    .pathParam("id", resource.getId())
                    .when()
                    .get("/api/v1/resource/{id}/key")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(is(resource.getKey()));
        }

        @Test
        void getResourceTypeById() {
            Resource resource = populateResource("Resource-Key-F",
                    "Resource-Type-F",
                    "Resource-Description-F");

            given()
                    .pathParam("id", resource.getId())
                    .when()
                    .get("/api/v1/resource/{id}/type")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(is(resource.getType()));
        }

        @Test
        void getResourceDescriptionById() {
            Resource resource = populateResource("Resource-Key-F",
                    "Resource-Type-F",
                    "Resource-Description-F");

            given()
                    .pathParam("id", resource.getId())
                    .when()
                    .get("/api/v1/resource/{id}/description")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body(is(resource.getDescription()));
        }
    }


    /*   PRIVATE   */

    private Client populateClient(String clientKey, String clientSecret, String clientDescription) {
        /* Note: RestTemplateBuilder and TestRestTemplate are utilized. */

        // ACT: Post request
        ClientPostRequest request = ClientPostRequest.builder()
                .key(clientKey)
                .description(clientDescription)
                .secret(clientSecret)
                .build();

        ResponseEntity<ClientResponse> entity = restTemplate.postForEntity(
                "/api/v1/client", request, ClientResponse.class);

        // ASSERT: Verify creation
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ClientResponse response = entity.getBody();

        return Client.builder()
                .id(response.id())
                .key(response.key())
                .description(response.description())
                .createdAt(response.createdAt())
                .updatedAt(response.updatedAt())
                .build();
    }

    private Resource populateResource(String resourceKey, String resourceType, String resourceDescription) {
        /* Note: RestTemplateBuilder and TestRestTemplate are utilized. */

        // ACT: Post request
        ResourcePutRequest request = ResourcePutRequest.builder()
                .key(resourceKey)
                .type(resourceType)
                .description(resourceDescription)
                .build();

        ResponseEntity<ResourceResponse> entity = restTemplate.postForEntity(
                "/api/v1/resource", request, ResourceResponse.class);

        // ASSERT: Verify creation
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResourceResponse response = entity.getBody();

        return Resource.builder()
                .id(response.id())
                .key(response.key())
                .type(response.type())
                .description(response.description())
                .createdAt(response.createdAt())
                .updatedAt(response.updatedAt())
                .build();
    }


}
