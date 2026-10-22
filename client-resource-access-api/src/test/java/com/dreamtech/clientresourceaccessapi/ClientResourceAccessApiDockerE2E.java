package com.dreamtech.clientresourceaccessapi;

import com.dreamtech.clientresourceaccessapi.dto.*;
import com.dreamtech.clientresourceaccessapi.model.Client;
import com.dreamtech.clientresourceaccessapi.model.ClientResourceAccess;
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
import static java.text.MessageFormat.format;
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
                .withUsername("e2e_username")         // dummy-username used by container to create Username
                .withPassword("secure_e2e_password"); // dummy-password used by container to create Password
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
            String clientKey = "createClient-Client-Key";
            String clientSecret = "createClient-VeryStrongSecretPassword";
            String clientDescription = "createClient-Client Description";

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
        void createClient_with_existing_key() {
            String clientKey = "createClient_with_existing_key-Client-Key";

            ClientPostRequest request = ClientPostRequest.builder()
                    .key(clientKey)
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
                    .body("description", nullValue());

            // duplicate - post with existing key
            given()
                    .contentType("application/json")
                    .body(request)
                    .when()
                    .post("/api/v1/client")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.CONFLICT.value())
                    .body("status", equalTo(409))
                    .body("error", containsString("Conflict"))
                    .body("path", equalTo("/api/v1/client"));
        }

        @Test
        void createResource() {
            String resourceKey = "createResource-Resource-Key";
            String resourceType = "createResource-Resource-Type";
            String resourceDescription = "createResource-Resource Description";

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

        @Test
        void createResource_with_existing_key() {
            String resourceKey = "createResource_with_existing_key-Resource-Key";
            String resourceType = "createResource_with_existing_key-Resource-Type";

            ResourcePostRequest request = ResourcePostRequest.builder()
                    .key(resourceKey)
                    .type(resourceType)
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
                    .body("description", nullValue());

            // duplicate - with existing key
            given()
                    .contentType("application/json")
                    .body(request)
                    .when()
                    .post("/api/v1/resource")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.CONFLICT.value())
                    .body("status", equalTo(409))
                    .body("error", equalTo("Conflict"))
                    .body("path", equalTo("/api/v1/resource"));
        }

        @Test
        void createClientResourceAccess() {
            Client client = populateClient("createClientResourceAccess-Client-Key-A",
                    "createClientResourceAccess-Client-Secret-A",
                    "createClientResourceAccess-Client-Description-A");

            Resource resource = populateResource("createClientResourceAccess-Resource-Key-A",
                    "createClientResourceAccess-Resource-Type-A",
                    "createClientResourceAccess-Resource-Description-A");

            String access_code = "createClientResourceAccess-Access-Code-A";
            String description = "createClientResourceAccess-Description-A";

            ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                    .clientId(client.getId())
                    .resourceId(resource.getId())
                    .accessCode(access_code)
                    .description(description)
                    .build();

            given()
                    .contentType("application/json")
                    .body(request)
                    .when()
                    .post("/api/v1/client-resource-access")
                    .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("clientId", equalTo(request.clientId().intValue()))
                    .body("resourceId", equalTo(request.resourceId().intValue()))
                    .body("accessCode", equalTo(request.accessCode()))
                    .body("description", equalTo(request.description()));

        }

        @Test
        void createClientResourceAccess_invalid_client_id() {
            Client client = populateClient("createClientResourceAccess_invalid_client_id-Client-Key-A",
                    "createClientResourceAccess_invalid_client_id-Client-Secret-A",
                    "createClientResourceAccess_invalid_client_id-Client-Description-A");

            Resource resource = populateResource("createClientResourceAccess_invalid_client_id-Resource-Key-A",
                    "createClientResourceAccess_invalid_client_id-Resource-Type-A",
                    "createClientResourceAccess_invalid_client_id-Resource-Description-A");

            String access_code = "createClientResourceAccess_invalid_client_id-Access-Code-A";
            String description = "createClientResourceAccess_invalid_client_id-Description-A";
            long faked_id = client.getId() + client.getId();

            ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                    .clientId(faked_id)
                    .resourceId(resource.getId())
                    .accessCode(access_code)
                    .description(description)
                    .build();

            given()
                    .contentType("application/json")
                    .body(request)
                    .when()
                    .post("/api/v1/client-resource-access")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("status", equalTo(404))
                    .body("error", equalTo("Not Found"))
                    .body("path", equalTo("/api/v1/client-resource-access"));
        }

        @Test
        void createClientResourceAccess_invalid_resource_id() {
            Client client = populateClient("createClientResourceAccess_invalid_resource_id-Client-Key-A",
                    "createClientResourceAccess_invalid_resource_id-Client-Secret-A",
                    "createClientResourceAccess_invalid_resource_id-Client-Description-A");

            Resource resource = populateResource("createClientResourceAccess_invalid_resource_id-Resource-Key-A",
                    "createClientResourceAccess_invalid_resource_id-Resource-Type-A",
                    "createClientResourceAccess_invalid_resource_id-Resource-Description-A");

            String access_code = "createClientResourceAccess_invalid_client_id-Access-Code-A";
            String description = "createClientResourceAccess_invalid_client_id-Description-A";
            long faked_id = resource.getId() + resource.getId();

            ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                    .clientId(client.getId())
                    .resourceId(faked_id)
                    .accessCode(access_code)
                    .description(description)
                    .build();

            given()
                    .contentType("application/json")
                    .body(request)
                    .when()
                    .post("/api/v1/client-resource-access")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("status", equalTo(404))
                    .body("error", equalTo("Not Found"))
                    .body("path", equalTo("/api/v1/client-resource-access"));
        }
    }


    @Nested
    class PatchAction {
        @Test
        void updateClient() {
            Client client = populateClient("updateClient-Client-Key-A",
                    "updateClient-VeryStrongSecrectPassword-A",
                    "updateClient-Client-Description-A");

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
        void updateClient_invalid_id() {
            Client client = populateClient("updateClient_invalid_id-Client-Key-A",
                    "updateClient_invalid_id-VeryStrongSecrectPassword-A",
                    "updateClient_invalid_id-Client-Description-A");

            long faked_id = client.getId() + client.getId();

            ClientPatchRequest request = ClientPatchRequest.builder()
                    .key(client.getKey())
                    .description("UPDATED" + client.getDescription())
                    .build();

            given()
                    .contentType("application/json")
                    .pathParam("id", faked_id)
                    .body(request)
                    .when()
                    .patch("/api/v1/client/{id}")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("status", equalTo(404))
                    .body("error", equalTo("Not Found"))
                    .body("path", equalTo(String.format("/api/v1/client/%s",faked_id)));
        }

        @Test
        void updateClient_with_existing_key() {
            Client client_A = populateClient("updateClient_with_existing_key-Client-Key-A",
                    "updateClient_with_existing_key-VeryStrongSecrectPassword-A",
                    "updateClient_with_existing_key-Client-Description-A");

            Client client_B = populateClient("updateClient_with_existing_key-Client-Key-B",
                    "updateClient_with_existing_key-VeryStrongSecrectPassword-B",
                    "updateClient_with_existing_key-Client-Description-B");

            ClientPatchRequest request = ClientPatchRequest.builder()
                    .key(client_A.getKey())
                    .description("UPDATED" + client_A.getDescription())
                    .build();

            given()
                    .contentType("application/json")
                    .pathParam("id", client_B.getId())
                    .body(request)
                    .when()
                    .patch("/api/v1/client/{id}")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.CONFLICT.value())
                    .body("status", equalTo(409))
                    .body("error", equalTo("Conflict"))
                    .body("path", equalTo(String.format("/api/v1/client/%s",client_B.getId())));
        }

        @Test
        void updateResource() {
            Resource resource = populateResource("updateResource-Resource-Key-A",
                    "updateResource-Resource-Type-A",
                    "updateResource-Resource-Description-A");

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

        @Test
        void updateResource_invalid_id() {
            Resource resource = populateResource("updateResource_invalid_id-Resource-Key-A",
                    "updateResource_invalid_id-Resource-Type-A",
                    "updateResource_invalid_id-Resource-Description-A");

            long faked_id = resource.getId() + resource.getId();

            ResourcePatchRequest request = ResourcePatchRequest.builder()
                    .key(resource.getKey())
                    .type("Will-Be_Updated-To-" + resource.getType())
                    .description(resource.getDescription() + "-To-Be-Updated")
                    .build();

            given()
                    .contentType("application/json")
                    .pathParam("id", faked_id)
                    .body(request)
                    .when()
                    .patch("/api/v1/resource/{id}")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("status", equalTo(404))
                    .body("error", equalTo("Not Found"))
                    .body("path", equalTo(String.format("/api/v1/resource/%s",faked_id)));
        }

        @Test
        void updateResource_with_existing_key() {
            Resource resource_A = populateResource("updateResource_with_existing_key-Resource-Key-A",
                    "updateResource_with_existing_key-Resource-Type-A",
                    "updateResource_with_existing_key-Resource-Description-A");

            Resource resource_B = populateResource("updateResource_with_existing_key-Resource-Key-B",
                    "updateResource_with_existing_key-Resource-Type-B",
                    "updateResource_with_existing_key-Resource-Description-B");

            ResourcePatchRequest request = ResourcePatchRequest.builder()
                    .key(resource_A.getKey())
                    .type("Will-Be_Updated-To-" + resource_A.getType())
                    .description(resource_A.getDescription() + "-To-Be-Updated")
                    .build();

            given()
                    .contentType("application/json")
                    .pathParam("id", resource_B.getId())
                    .body(request)
                    .when()
                    .patch("/api/v1/resource/{id}")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.CONFLICT.value())
                    .body("status", equalTo(409))
                    .body("error", equalTo("Conflict"))
                    .body("path", equalTo(String.format("/api/v1/resource/%s",resource_B.getId())));
        }

        @Test
        void updateClientResourceAccess() {
            Client client = populateClient("updateClientResourceAccess-Client-Key-A",
                    "updateClientResourceAccess-VeryStrongSecrectPassword-A",
                    "updateClientResourceAccess-Client-Description-A");

            Resource resource = populateResource("updateClientResourceAccess-Resource-Key-A",
                    "updateClientResourceAccess-Resource-Type-A",
                    "updateClientResourceAccess-Resource-Description-A");

            String access_code = "updateClientResourceAccess-AccessCode-A";
            String description = "updateClientResourceAccess-Description-A";

            ClientResourceAccess cra = populateClientResourceAccess(client.getId(), resource.getId(), access_code, description);

            ClientResourceAccessPatchRequest request = ClientResourceAccessPatchRequest.builder()
                    .accessCode("Updated-" + access_code)
                    .description(description + "-To-Be-Updated")
                    .build();

            given()
                    .contentType("application/json")
                    .pathParam("clientId", client.getId())
                    .pathParam("resourceId", resource.getId())
                    .body(request)
                    .when()
                    .patch("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("clientId", equalTo(cra.getClientId().intValue()))
                    .body("resourceId", equalTo(cra.getResourceId().intValue()))
                    .body("accessCode", equalTo(request.accessCode()))
                    .body("description", equalTo(request.description()));
        }

        @Test
        void updateClientResourceAccess_invalid_client() {
            Client client = populateClient("updateClientResourceAccess_invalid_client-Client-Key-A",
                    "updateClientResourceAccess_invalid_client-VeryStrongSecrectPassword-A",
                    "updateClientResourceAccess_invalid_client-Client-Description-A");

            Resource resource = populateResource("updateClientResourceAccess_invalid_client-Resource-Key-A",
                    "updateClientResourceAccess_invalid_client-Resource-Type-A",
                    "updateClientResourceAccess_invalid_client-Resource-Description-A");

            String access_code = "updateClientResourceAccess_invalie_client-AccessCode-A";
            String description = "updateClientResourceAccess_invalie_client-Description-A";

            ClientResourceAccess cra = populateClientResourceAccess(client.getId(), resource.getId(), access_code, description);

            long faked_id = client.getId() + client.getId();

            ClientResourceAccessPatchRequest request = ClientResourceAccessPatchRequest.builder()
                    .accessCode("Updated-" + access_code)
                    .description(description + "-To-Be-Updated")
                    .build();

            given()
                    .contentType("application/json")
                    .pathParam("clientId", faked_id)
                    .pathParam("resourceId", cra.getResourceId())
                    .body(request)
                    .when()
                    .patch("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("status", equalTo(404))
                    .body("error", equalTo("Not Found"))
                    .body("path", equalTo(format("/api/v1/client-resource-access/client/{0}/resource/{1}",
                            String.valueOf(faked_id), String.valueOf(resource.getId()))));
        }

        @Test
        void updateClientResourceAccess_invalid_resource() {
            Client client = populateClient("updateClientResourceAccess_invalid_resource-Client-Key-A",
                    "updateClientResourceAccess_invalid_resource-VeryStrongSecrectPassword-A",
                    "updateClientResourceAccess_invalid_resource-Client-Description-A");

            Resource resource = populateResource("updateClientResourceAccess_invalid_resource-Resource-Key-A",
                    "updateClientResourceAccess_invalid_resource-Resource-Type-A",
                    "updateClientResourceAccess_invalid_resource-Resource-Description-A");

            String access_code = "updateClientResourceAccess_invalid_resource-AccessCode-A";
            String description = "updateClientResourceAccess_invalid_resource-Description-A";

            ClientResourceAccess cra = populateClientResourceAccess(client.getId(), resource.getId(), access_code, description);

            long faked_id = resource.getId()+ resource.getId();

            ClientResourceAccessPatchRequest request = ClientResourceAccessPatchRequest.builder()
                    .accessCode("Updated-" + access_code)
                    .description(description + "-To-Be-Updated")
                    .build();

            given()
                    .contentType("application/json")
                    .pathParam("clientId", cra.getClientId())
                    .pathParam("resourceId", faked_id)
                    .body(request)
                    .when()
                    .patch("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("status", equalTo(404))
                    .body("error", equalTo("Not Found"))
                    .body("path", equalTo(format("/api/v1/client-resource-access/client/{0}/resource/{1}",
                            String.valueOf(client.getId()), String.valueOf(faked_id))));
        }
    }


    @Nested
    class PutAction {
        @Test
        void replaceClient_existing_id() {
            Client client = populateClient("replaceClient_existing_id-Client-Key-B",
                    "replaceClient_existing_id-VeryStrongSecrectPassword-B",
                    "replaceClient_existing_id-Client-Description-B");

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
        void replaceClient_new_id() {
            ClientPutRequest request = ClientPutRequest.builder()
                    .key("replaceClient_new_id-Client-Key-C-Replaced")
                    .description("replaceClient_new_id-REPLACED-Client-Descriprion-C")
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
        void replaceClient_with_existing_key() {
            Client client_A =populateClient("replaceClient_with_existing_key-Client-Key-A",
                    "replaceClient_with_existing_key-VeryStrongSecrectPassword-A",
                    "replaceClient_with_existing_key-Client-Description-A");

            Client client_B = populateClient("replaceClient_with_existing_key-Client-Key-B",
                    "replaceClient_with_existing_key-VeryStrongSecrectPassword-B",
                    "replaceClient_with_existing_key-Client-Description-B");

            ClientPutRequest request = ClientPutRequest.builder()
                    .key(client_A.getKey())
                    .description("REPLACED-" + client_A.getDescription())
                    .build();

            given()
                    .contentType("application/json")
                    .pathParam("id", client_B.getId())
                    .body(request)
                    .when()
                    .put("/api/v1/client/{id}")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.CONFLICT.value())
                    .body("status", equalTo(409))
                    .body("error", equalTo("Conflict"))
                    .body("path", equalTo(String.format("/api/v1/client/%s", client_B.getId())));
        }

        @Test
        void replaceResource_existing_id() {
            Resource resource = populateResource("replaceResource_existing_id-Resource-Key-B",
                    "replaceResource_existing_id-Resource-Type-B",
                    "replaceResource_existing_id-Resource-Description-B");

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
        void replaceResource_new_id() {
            ResourcePutRequest request = ResourcePutRequest.builder()
                    .key("replaceResource_new_id-Resource-Key-C-Replaced")
                    .type("replaceResource_new_id-Will-Be_Replaced-To-Resource-Type-C")
                    .description("replaceResource_new_id-Resource-Description-C-To-Be-Replaced")
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

        @Test
        void replaceResource_with_existing_key() {
            Resource resource_A = populateResource("replaceResource_with_existing_key-Resource-Key-A",
                    "replaceResource_with_existing_key-Resource-Type-A",
                    "replaceResource_with_existing_key-Resource-Description-A");

            Resource resource_B = populateResource("replaceResource_with_existing_key-Resource-Key-B",
                    "replaceResource_with_existing_key-Resource-Type-B",
                    "replaceResource_with_existing_key-Resource-Description-B");

            ResourcePutRequest request = ResourcePutRequest.builder()
                    .key(resource_A.getKey())
                    .type("Will-Be_Replaced-To-" + resource_B.getType())
                    .description(resource_B.getDescription() + "-To-Be-Replaced")
                    .build();

            given()
                    .contentType("application/json")
                    .pathParam("id", resource_B.getId())
                    .body(request)
                    .when()
                    .put("/api/v1/resource/{id}")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.CONFLICT.value())
                    .body("status", equalTo(409))
                    .body("error", equalTo("Conflict"))
                    .body("path", equalTo(String.format("/api/v1/resource/%s", resource_B.getId())));
        }

        @Test
        void replaceClientResourceAccess_existing_ids() {
            Client client =populateClient("replaceClientResourceAccess_existing_ids-Client-Key-A",
                    "replaceClientResourceAccess_existing_ids-VeryStrongSecrectPassword-A",
                    "replaceClientResourceAccess_existing_ids-Client-Description-A");

            Resource resource = populateResource("replaceClientResourceAccess_existing_ids-Resource-Key-A",
                    "replaceClientResourceAccess_existing_ids-Resource-Type-A",
                    "replaceClientResourceAccess_existing_ids-Resource-Description-A");

            String access_code = "replaceClientResourceAccess_existing_ids-AccessCode-A";
            String description = "replaceClientResourceAccess_existing_ids-Description-A";

            ClientResourceAccess cra = populateClientResourceAccess(client.getId(), resource.getId(), access_code, description);

            ClientResourceAccessPutRequest request = ClientResourceAccessPutRequest.builder()
                    .accessCode("Will-Be_Replaced-To-" + access_code)
                    .description(description + "-To-Be-Replaced")
                    .build();

            given()
                    .contentType("application/json")
                    .pathParam("clientId", cra.getClientId())
                    .pathParam("resourceId", cra.getResourceId())
                    .body(request)
                    .when()
                    .put(("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}"))
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("clientId", equalTo(client.getId().intValue()))
                    .body("resourceId", equalTo(resource.getId().intValue()))
                    .body("accessCode", equalTo(request.accessCode()))
                    .body("description", equalTo(request.description()));
        }

        @Test
        void replaceClientResourceAccess_new_ids() {
            Client client =populateClient("replaceClientResourceAccess_new_ids-Client-Key-A",
                    "replaceClientResourceAccess_new_ids-VeryStrongSecrectPassword-A",
                    "replaceClientResourceAccess_new_ids-Client-Description-A");

            Resource resource = populateResource("replaceClientResourceAccess_new_ids-Resource-Key-A",
                    "replaceClientResourceAccess_new_ids-Resource-Type-A",
                    "replaceClientResourceAccess_new_ids-Resource-Description-A");

            String access_code = "replaceClientResourceAccess_new_ids-AccessCode-A";
            String description = "replaceClientResourceAccess_new_ids-Description-A";

            ClientResourceAccessPutRequest request = ClientResourceAccessPutRequest.builder()
                    .accessCode("Will-Be_Replaced-To-" + access_code)
                    .description(description + "-To-Be-Replaced")
                    .build();

            given()
                    .contentType("application/json")
                    .pathParam("clientId", client.getId())
                    .pathParam("resourceId", resource.getId())
                    .body(request)
                    .when()
                    .put(("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}"))
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("clientId", equalTo(client.getId().intValue()))
                    .body("resourceId", equalTo(resource.getId().intValue()))
                    .body("accessCode", equalTo(request.accessCode()))
                    .body("description", equalTo(request.description()));
        }

        @Test
        void replaceClientResourceAccess_invalid_client() {
            Client client =populateClient("replaceClientResourceAccess_invalid_client-Client-Key-A",
                    "replaceClientResourceAccess_invalid_client-VeryStrongSecrectPassword-A",
                    "replaceClientResourceAccess_invalid_client-Client-Description-A");

            Resource resource = populateResource("replaceClientResourceAccess_invalid_client-Resource-Key-A",
                    "replaceClientResourceAccess_invalid_client-Resource-Type-A",
                    "replaceClientResourceAccess_invalid_client-Resource-Description-A");

            String access_code = "replaceClientResourceAccess_invalid_client-AccessCode-A";
            String description = "replaceClientResourceAccess_invalid_client-Description-A";
            long faked_id = client.getId() * 123456L;

            ClientResourceAccessPutRequest request = ClientResourceAccessPutRequest.builder()
                    .accessCode("Will-Be_Replaced-To-" + access_code)
                    .description(description + "-To-Be-Replaced")
                    .build();

            given()
                    .contentType("application/json")
                    .pathParam("clientId", faked_id)
                    .pathParam("resourceId", resource.getId())
                    .body(request)
                    .when()
                    .put(("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}"))
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("status", equalTo(404))
                    .body("error", equalTo("Not Found"))
                    .body("path", equalTo(format("/api/v1/client-resource-access/client/{0}/resource/{1}",
                            String.valueOf(faked_id), String.valueOf(resource.getId()))));
        }

        @Test
        void replaceClientResourceAccess_invalid_resource() {
            Client client =populateClient("replaceClientResourceAccess_invalid_resource-Client-Key-A",
                    "replaceClientResourceAccess_invalid_resource-VeryStrongSecrectPassword-A",
                    "replaceClientResourceAccess_invalid_resource-Client-Description-A");

            Resource resource = populateResource("replaceClientResourceAccess_invalid_resource-Resource-Key-A",
                    "replaceClientResourceAccess_invalid_resource-Resource-Type-A",
                    "replaceClientResourceAccess_invalid_resource-Resource-Description-A");

            String access_code = "replaceClientResourceAccess_invalid_resource-AccessCode-A";
            String description = "replaceClientResourceAccess_invalid_resource-Description-A";
            long faked_id = resource.getId() * 123456L;

            ClientResourceAccessPutRequest request = ClientResourceAccessPutRequest.builder()
                    .accessCode("Will-Be_Replaced-To-" + access_code)
                    .description(description + "-To-Be-Replaced")
                    .build();

            given()
                    .contentType("application/json")
                    .pathParam("clientId", client.getId())
                    .pathParam("resourceId", faked_id)
                    .body(request)
                    .when()
                    .put(("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}"))
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("status", equalTo(404))
                    .body("error", equalTo("Not Found"))
                    .body("path", equalTo(format("/api/v1/client-resource-access/client/{0}/resource/{1}",
                            String.valueOf(client.getId()), String.valueOf(faked_id))));
        }

    }


    @Nested
    class DeleteAction {
        @Test
        void deleteClientById() {
            Client client = populateClient("deleteClientById-Client-Key-D",
                    "deleteClientById-VeryStrongSecrectPassword-D",
                    "deleteClientById-Client-Description-D");

            given()
                    .pathParam("id", client.getId())
                    .when()
                    .delete("/api/v1/client/{id}")
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        void deleteClientById_invalid_id() {
            Client client = populateClient("deleteClientById_invalid_id-Client-Key-D",
                    "deleteClientById_invalid_id-VeryStrongSecrectPassword-D",
                    "deleteClientById_invalid_id-Client-Description-D");

            long faked_id = client.getId() + client.getId();

            given()
                    .pathParam("id", faked_id)
                    .when()
                    .delete("/api/v1/client/{id}")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("status", equalTo(404))
                    .body("error", equalTo("Not Found"))
                    .body("path", equalTo(String.format("/api/v1/client/%s", faked_id)));
        }

        @Test
        void deleteClient_unprossable() {
            Client client = populateClient("deleteClient_unprossable-Client-Key-A",
                    "deleteClient_unprossable-VeryStrongSecrectPassword-A",
                    "deleteClient_unprossable-Client-Description-A");

            Resource resource = populateResource("deleteClient_unprossable-Resource-Key-A",
                    "deleteClient_unprossable-Resource-Type-A",
                    "deleteClient_unprossable-Resource-Description-A");

            String access_code = "deleteClient_unprossable-AccessCode-A";
            String description = "deleteClient_unprossable-Description-A";
            ClientResourceAccess cra = populateClientResourceAccess(client.getId(), resource.getId(), access_code, description);

            given()
                    .pathParam("id", cra.getClientId())
                    .when()
                    .delete("/api/v1/client/{id}")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.UNPROCESSABLE_CONTENT.value())
                    .body("status", equalTo(422))
                    .body("error", equalTo("Unprocessable Content"))
                    .body("path", equalTo(format("/api/v1/client/{0}", String.valueOf(cra.getClientId()))));
        }


        @Test
        void deleteResourceById() {
            Resource resource = populateResource("deleteResourceById-Resource-Key-D",
                    "deleteResourceById-Resource-Type-D",
                    "deleteResourceById-Resource-Description-D");

            given()
                    .pathParam("id", resource.getId())
                    .when()
                    .delete("/api/v1/resource/{id}")
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        void deleteResourceById_invalid_id() {
            Resource resource = populateResource("deleteResourceById_invalid_id-Resource-Key-D",
                    "deleteResourceById_invalid_id-Resource-Type-D",
                    "deleteResourceById_invalid_id-Resource-Description-D");

            long faked_id = resource.getId() + resource.getId();

            given()
                    .pathParam("id", faked_id)
                    .when()
                    .delete("/api/v1/resource/{id}")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("status", equalTo(404))
                    .body("error", equalTo("Not Found"))
                    .body("path", equalTo(String.format("/api/v1/resource/%s", faked_id)));
        }

        @Test
        void deleteResource_unprossable() {
            Client client = populateClient("deleteResource_unprossable-Client-Key-A",
                    "deleteResource_unprossable-VeryStrongSecrectPassword-A",
                    "deleteResource_unprossable-Client-Description-A");

            Resource resource = populateResource("deleteResource_unprossable-Resource-Key-A",
                    "deleteResource_unprossable-Resource-Type-A",
                    "deleteResource_unprossable-Resource-Description-A");

            String access_code = "deleteResource_unprossable-AccessCode-A";
            String description = "deleteClient_unprossable-Description-A";
            ClientResourceAccess cra = populateClientResourceAccess(client.getId(), resource.getId(), access_code, description);

            given()
                    .pathParam("id", cra.getResourceId())
                    .when()
                    .delete("/api/v1/client/{id}")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.UNPROCESSABLE_CONTENT.value())
                    .body("status", equalTo(422))
                    .body("error", equalTo("Unprocessable Content"))
                    .body("path", equalTo(format("/api/v1/client/{0}", String.valueOf(cra.getResourceId()))));
        }


        @Test
        void deleteClientResourceAccess() {
            Client client =populateClient("deletdeleteClientResourceAccesst_unprossable-Client-Key-A",
                    "deletdeleteClientResourceAccesst_unprossable-VeryStrongSecrectPassword-A",
                    "deletdeleteClientResourceAccesst_unprossable-Client-Description-A");

            Resource resource = populateResource("deletdeleteClientResourceAccesst_unprossable-Resource-Key-A",
                    "deletdeleteClientResourceAccesst_unprossable-Resource-Type-A",
                    "deletdeleteClientResourceAccesst_unprossable-Resource-Description-A");

            String access_code = "deletdeleteClientResourceAccesst_unprossable-AccessCode-A";
            String description = "deletdeleteClientResourceAccesst_unprossable-Description-A";

            ClientResourceAccess cra = populateClientResourceAccess(client.getId(), resource.getId(), access_code, description);

            given()
                    .contentType("application/json")
                    .pathParam("clientId", cra.getClientId())
                    .pathParam("resourceId", cra.getResourceId())
                    .when()
                    .delete(("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}"))
                    .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        void deleteClientResourceAccess_invalid_client() {
            Client client =populateClient("deleteClientResourceAccess_invalid_client-Client-Key-A",
                    "deleteClientResourceAccess_invalid_client-VeryStrongSecrectPassword-A",
                    "deleteClientResourceAccess_invalid_client-Client-Description-A");

            Resource resource = populateResource("deleteClientResourceAccess_invalid_client-Resource-Key-A",
                    "deleteClientResourceAccess_invalid_client-Resource-Type-A",
                    "deleteClientResourceAccess_invalid_client-Resource-Description-A");

            String access_code = "deleteClientResourceAccess_invalid_client-AccessCode-A";
            String description = "deleteClientResourceAccess_invalid_client-Description-A";

            ClientResourceAccess cra = populateClientResourceAccess(client.getId(), resource.getId(), access_code, description);

            long faked_id = cra.getClientId() * 123456L;

            given()
                    .contentType("application/json")
                    .pathParam("clientId", faked_id)
                    .pathParam("resourceId", cra.getResourceId())
                    .when()
                    .delete(("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}"))
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("status", equalTo(404))
                    .body("error", equalTo("Not Found"))
                    .body("path", equalTo(format("/api/v1/client-resource-access/client/{0}/resource/{1}",
                            String.valueOf(faked_id), String.valueOf(cra.getResourceId()))));
        }

        @Test
        void deleteClientResourceAccess_invalid_resource() {
            Client client =populateClient("deleteClientResourceAccess_invalid_resource-Client-Key-A",
                    "deleteClientResourceAccess_invalid_resource-VeryStrongSecrectPassword-A",
                    "deleteClientResourceAccess_invalid_resource-Client-Description-A");

            Resource resource = populateResource("deleteClientResourceAccess_invalid_resource-Resource-Key-A",
                    "deleteClientResourceAccess_invalid_resource-Resource-Type-A",
                    "deleteClientResourceAccess_invalid_resource-Resource-Description-A");

            String access_code = "deleteClientResourceAccess_invalid_resource-AccessCode-A";
            String description = "deleteClientResourceAccess_invalid_resource-Description-A";

            ClientResourceAccess cra = populateClientResourceAccess(client.getId(), resource.getId(), access_code, description);

            long faked_id = cra.getResourceId() * 123456L;

            given()
                    .contentType("application/json")
                    .pathParam("clientId", cra.getClientId())
                    .pathParam("resourceId", faked_id)
                    .when()
                    .delete(("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}"))
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("status", equalTo(404))
                    .body("error", equalTo("Not Found"))
                    .body("path", equalTo(format("/api/v1/client-resource-access/client/{0}/resource/{1}",
                            String.valueOf(cra.getClientId()), String.valueOf(faked_id))));
        }
    }


    @Nested
    class GetAction {
        @Test
        void getClientById_invalid_id() {
            Client client = populateClient("getClientById_invalid_id-lient-Key-E",
                    "getClientById_invalid_id-VeryStrongSecrectPassword-E",
                    "getClientById_invalid_id-Client-Description-E");

            long faked_id = client.getId() + client.getId();

            given()
                    .pathParam("id", faked_id)
                    .when()
                    .get("/api/v1/client/{id}")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("status", equalTo(404))
                    .body("error", equalTo("Not Found"))
                    .body("path", equalTo(String.format("/api/v1/client/%s", faked_id)));
        }

        @Test
        void getClientById() {
            Client client = populateClient("getClientById-Client-Key-E",
                    "getClientById-VeryStrongSecrectPassword-E",
                    "getClientById-Client-Description-E");

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
            Client client = populateClient("getClientKey-ByIdClient-Key-F",
                    "getClientKeyById-VeryStrongSecrectPassword-F",
                    "getClientKeyById-Client-Description-F");

            given()
                    .pathParam("id", client.getId())
                    .when()
                    .get("/api/v1/client/{id}/key")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("text", equalTo(client.getKey()));
        }

        @Test
        void getClientDescriptionById() {
            Client client = populateClient("getClientDescriptionById-Client-Key-F",
                    "getClientDescriptionById-VeryStrongSecrectPassword-F",
                    "getClientDescriptionById-Client-Description-F");

            given()
                    .pathParam("id", client.getId())
                    .when()
                    .get("/api/v1/client/{id}/description")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("text", equalTo(client.getDescription()));
        }

        @Test
        void getClientDescriptionById_null_description() {
            Client client = populateClient("getClientDescriptionById_null_description-Client-Key-F",
                    "getClientDescriptionById_null_description-VeryStrongSecrectPassword-F",
                    null);

            given()
                    .pathParam("id", client.getId())
                    .when()
                    .get("/api/v1/client/{id}/description")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("text", equalTo(client.getDescription()))
                    .body("text", nullValue());
        }

        @Test
        void getResourceById_invalid_id() {
            Resource resource = populateResource("getResourceById_invalid_id-Resource-Key-E",
                    "getResourceById_invalid_id-Resource-Type-E",
                    "getResourceById_invalid_id-Resource-Description-E");

            long faked_id = resource.getId() + resource.getId();

            given()
                    .pathParam("id", faked_id)
                    .when()
                    .get("/api/v1/resource/{id}")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("status", equalTo(404))
                    .body("error", equalTo("Not Found"))
                    .body("path", equalTo(String.format("/api/v1/resource/%s", faked_id)));
        }

        @Test
        void getResourceById() {
            Resource resource = populateResource("getResourceById-Resource-Key-E",
                    "getResourceById-Resource-Type-E",
                    "getResourceById-Resource-Description-E");

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
            Resource resource = populateResource("getResourceKeyById-Resource-Key-F",
                    "getResourceKeyById-Resource-Type-F",
                    "getResourceKeyById-Resource-Description-F");

            given()
                    .pathParam("id", resource.getId())
                    .when()
                    .get("/api/v1/resource/{id}/key")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("text", equalTo(resource.getKey()));
        }

        @Test
        void getResourceTypeById() {
            Resource resource = populateResource("getResourceTypeById-Resource-Key-F",
                    "getResourceTypeById-Resource-Type-F",
                    "getResourceTypeById-Resource-Description-F");

            given()
                    .pathParam("id", resource.getId())
                    .when()
                    .get("/api/v1/resource/{id}/type")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("text", equalTo(resource.getType()));
        }

        @Test
        void getResourceDescriptionById() {
            Resource resource = populateResource("getResourceDescriptionById-Resource-Key-F",
                    "getResourceDescriptionById-Resource-Type-F",
                    "getResourceDescriptionById-Resource-Description-F");

            given()
                    .pathParam("id", resource.getId())
                    .when()
                    .get("/api/v1/resource/{id}/description")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("text", equalTo(resource.getDescription()));
        }

        @Test
        void getResourceDescriptionById_null_description() {
            Resource resource = populateResource("getResourceDescriptionById_null_description-Resource-Key-F",
                    "getResourceDescriptionById_null_description-Resource-Type-F",
                    null);

            given()
                    .pathParam("id", resource.getId())
                    .when()
                    .get("/api/v1/resource/{id}/description")
                    .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("text", equalTo(resource.getDescription()))
                    .body("text", nullValue());
        }

        @Test
        void getClientResourceAccessByIds_invalid_client() {
            Client client = populateClient("getClientResourceAccessByIds_invalid_client-Client-Key-A",
                    "getClientResourceAccessByIds_invalid_client-Client-SecrectPassword-A",
                    "getClientResourceAccessByIds_invalid_client-Client-Description-A");

            Resource resource = populateResource("getClientResourceAccessByIds_invalid_client-Resource-Key-A",
                    "getClientResourceAccessByIds_invalid_client-Resource-Type-A",
                    "getClientResourceAccessByIds_invalid_client-Resource-Description-A");

            String access_code = "getClientResourceAccessByIds_invalid_client-AccessCode-A";
            String description = "getClientResourceAccessByIds_invalid_client-Description-A";

            ClientResourceAccess cra = populateClientResourceAccess(client.getId(), resource.getId(), access_code, description);
            long faked_id = cra.getClientId() * 123456L;

            given()
                    .pathParam("clientId", faked_id)
                    .pathParam("resourceId", cra.getResourceId())
                    .when()
                    .get("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("status", equalTo(404))
                    .body("error", equalTo("Not Found"))
                    .body("path", equalTo(format("/api/v1/client-resource-access/client/{0}/resource/{1}",
                            String.valueOf(faked_id), String.valueOf(cra.getResourceId()))));
        }

        @Test
        void getClientResourceAccessByIds_invalid_resource() {
            Client client = populateClient("getClientResourceAccessByIds_invalid_resource-Client-Key-A",
                    "getClientResourceAccessByIds_invalid_resource-Client-SecrectPassword-A",
                    "getClientResourceAccessByIds_invalid_resource-Client-Description-A");

            Resource resource = populateResource("getClientResourceAccessByIds_invalid_resource-Resource-Key-A",
                    "getClientResourceAccessByIds_invalid_resource-Resource-Type-A",
                    "getClientResourceAccessByIds_invalid_resource-Resource-Description-A");

            String access_code = "getClientResourceAccessByIds_invalid_resource-AccessCode-A";
            String description = "getClientResourceAccessByIds_invalid_resource-Description-A";

            ClientResourceAccess cra = populateClientResourceAccess(client.getId(), resource.getId(), access_code, description);
            long faked_id = cra.getResourceId() * 123456L;

            given()
                    .pathParam("clientId", cra.getClientId())
                    .pathParam("resourceId", faked_id)
                    .when()
                    .get("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .body("status", equalTo(404))
                    .body("error", equalTo("Not Found"))
                    .body("path", equalTo(format("/api/v1/client-resource-access/client/{0}/resource/{1}",
                            String.valueOf(cra.getClientId()), String.valueOf(faked_id))));
        }

        @Test
        void getClientResourceAccessByIds() {
            Client client = populateClient("getClientResourceAccessByIds-Client-Key-A",
                    "getClientResourceAccessByIds-Client-SecrectPassword-A",
                    "getClientResourceAccessByIds-Client-Description-A");

            Resource resource = populateResource("getClientResourceAccessByIds-Resource-Key-A",
                    "getClientResourceAccessByIds-Resource-Type-A",
                    "getClientResourceAccessByIds-Resource-Description-A");

            String access_code = "getClientResourceAccessByIds-AccessCode-A";
            String description = "getClientResourceAccessByIds-Description-A";

            ClientResourceAccess cra = populateClientResourceAccess(client.getId(), resource.getId(), access_code, description);

            given()
                    .pathParam("clientId", cra.getClientId())
                    .pathParam("resourceId", cra.getResourceId())
                    .when()
                    .get("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("clientId", equalTo(cra.getClientId().intValue()))
                    .body("resourceId", equalTo(cra.getResourceId().intValue()))
                    .body("accessCode", equalTo(cra.getAccessCode()))
                    .body("description", equalTo(cra.getDescription()));
        }

        @Test
        void getClientResourceAccessByIds_AccessCode() {
            Client client = populateClient("getClientResourceAccessByIds_AccessCode-Client-Key-A",
                    "getClientResourceAccessByIds_AccessCode-Client-SecrectPassword-A",
                    "getClientResourceAccessByIds_AccessCode-Client-Description-A");

            Resource resource = populateResource("getClientResourceAccessByIds_AccessCode-Resource-Key-A",
                    "getClientResourceAccessByIds_AccessCode-Resource-Type-A",
                    "getClientResourceAccessByIds_AccessCode-Resource-Description-A");

            String access_code = "getClientResourceAccessByIds_AccessCode-AccessCode-A";
            String description = "getClientResourceAccessByIds_AccessCode-Description-A";

            ClientResourceAccess cra = populateClientResourceAccess(client.getId(), resource.getId(), access_code, description);

            given()
                    .pathParam("clientId", cra.getClientId())
                    .pathParam("resourceId", cra.getResourceId())
                    .when()
                    .get("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}/access-code")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("text", equalTo(cra.getAccessCode()));
        }

        @Test
        void getClientResourceAccessByIds_description() {
            Client client = populateClient("getClientResourceAccessByIds_description-Client-Key-A",
                    "getClientResourceAccessByIds_description-Client-SecrectPassword-A",
                    "getClientResourceAccessByIds_description-Client-Description-A");

            Resource resource = populateResource("getClientResourceAccessByIds_description-Resource-Key-A",
                    "getClientResourceAccessByIds_description-Resource-Type-A",
                    "getClientResourceAccessByIds_description-Resource-Description-A");

            String access_code = "getClientResourceAccessByIds_description-AccessCode-A";
            String description = "getClientResourceAccessByIds_description-Description-A";

            ClientResourceAccess cra = populateClientResourceAccess(client.getId(), resource.getId(), access_code, description);

            given()
                    .pathParam("clientId", cra.getClientId())
                    .pathParam("resourceId", cra.getResourceId())
                    .when()
                    .get("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}/description")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("text", equalTo(cra.getDescription()));
        }

        @Test
        void getClientResourceAccessByIds_null_description() {
            Client client = populateClient("getClientResourceAccessByIds_description-Client-Key-A",
                    "getClientResourceAccessByIds_description-Client-SecrectPassword-A",
                    "getClientResourceAccessByIds_description-Client-Description-A");

            Resource resource = populateResource("getClientResourceAccessByIds_description-Resource-Key-A",
                    "getClientResourceAccessByIds_description-Resource-Type-A",
                    "getClientResourceAccessByIds_description-Resource-Description-A");

            String access_code = "getClientResourceAccessByIds_description-AccessCode-A";

            ClientResourceAccess cra = populateClientResourceAccess(client.getId(), resource.getId(), access_code, null);

            given()
                    .pathParam("clientId", cra.getClientId())
                    .pathParam("resourceId", cra.getResourceId())
                    .when()
                    .get("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}/description")
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.OK.value())
                    .body("text", equalTo(cra.getDescription()))
                    .body("text", nullValue());
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


    private ClientResourceAccess populateClientResourceAccess(long clientId, long resourceId, String accessCode, String description) {
        /* Note: RestTemplateBuilder and TestRestTemplate are utilized. */

        // ACT: Post request
        ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                .clientId(clientId)
                .resourceId(resourceId)
                .accessCode(accessCode)
                .description(description)
                .build();

        ResponseEntity<ClientResourceAccessResponse> entity = restTemplate.postForEntity(
                "/api/v1/client-resource-access", request, ClientResourceAccessResponse.class);

        // ASSERT: Verify creation
        assertThat(entity.getBody()).isNotNull();
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ClientResourceAccessResponse response = entity.getBody();

        return ClientResourceAccess.builder()
                .clientId(response.clientId())
                .resourceId(response.resourceId())
                .accessCode(response.accessCode())
                .description(response.description())
                .build();
    }
}
