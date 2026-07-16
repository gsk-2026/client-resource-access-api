package com.dreamtech.clientresourceaccessapi;

import com.dreamtech.clientresourceaccessapi.dto.ClientResourceAccessPostRequest;
import com.dreamtech.clientresourceaccessapi.model.Client;
import com.dreamtech.clientresourceaccessapi.model.ClientResourceAccess;
import com.dreamtech.clientresourceaccessapi.model.Resource;
import com.dreamtech.clientresourceaccessapi.repository.ClientRepository;
import com.dreamtech.clientresourceaccessapi.repository.ClientResourceAccessRepository;
import com.dreamtech.clientresourceaccessapi.repository.ResourceRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
        import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
        import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
        import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2mem-test") // Loads application-h2mem-test.yml
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Forces Spring to use your YAML parameters
@AutoConfigureMockMvc
class ClientResourceAccessIT {
    /*  Integration Test for all Controllers Services Repositories.
        This is the Integration over all layers of :
                ClientController, ClientService, ClientRepository;
                ResourceController, ResourceService, ResourceRepository;
                ClientResourceAccessController, ClientResourceAccessService, ClientResourceAccessRepository;
        H2 in-memory database ia applied to support the Repositories Testing.
        Note: More test cases may be added later.   */

    @Autowired
    private MockMvc mockMvc;

    @Autowired private Environment environment;

    @Autowired  // Real ClientRepository to verify database table CLIENT
    private ClientRepository clientRepository;

    @Autowired  // Real ResourceRepository to verify database table DB_INFO
    private ResourceRepository resourceRepository;

    @Autowired  // Real ClientResourceAccessRepository to verify database table DB_CLIENT_AUTH
    private ClientResourceAccessRepository clientResourceAccessRepository;

    @Autowired
    private ObjectMapper objectMapper; // To serialize/deserialize JSON strings

    @Autowired
    private WebApplicationContext context; // Loads the full app context

    // The testing setup data
    private final String missingAccessCode = "Missing-Client-Access-Code";
    private final String missingDescription = "Missing-ClientResourceAccess-Description";

    private final String existingAccessCode = "Existing-Access-Code";
    private final String existingDescription = "Existing-ClientResourceAccess-Description";

    private Long existingClientId, existingResourceId, missingClientId, missingResourceId;


    @BeforeEach
    void setUpEach() {
        //
        String missingResourceKey = "Missing-Resource-Key";
        String missingClientKey = "Missing-Client-Key";
        String existingResourceKey = "EXISTING-RESOURCE-KEY";
        String existingClientKey = "EXSTING-CLIENT-KEY";


        // The real H2 in-memory database is used here.
        clientRepository.deleteAll();
        resourceRepository.deleteAll();
        clientResourceAccessRepository.deleteAll();

        // insert into CLIENT values (...)
        Client client_1 = new Client();
        client_1.setKey(existingClientKey);
        client_1 = clientRepository.save(client_1);
        existingClientId = client_1.getId();

        /* Note: CLIENT_RESOURCE_ACCESS has no record for this client_2 */
        Client client_2 = new Client();
        client_2.setKey(missingClientKey);
        client_2 = clientRepository.save(client_2);
        missingClientId = client_2.getId();

        // insert into RESOURCE values (...)
        Resource resource_1 = new Resource();
        resource_1.setKey(existingResourceKey);
        resource_1.setType("existingResourceType");
        resource_1 = resourceRepository.save(resource_1);
        existingResourceId = resource_1.getId();

        /* Note: CLIENT_RESOURCE_ACCESS has no record for this resource_2 */
        Resource resource_2 = new Resource();
        resource_2.setKey(missingResourceKey);
        resource_2.setType("missingResourceType");
        resource_2 = resourceRepository.save(resource_2);
        missingResourceId = resource_2.getId();

        // insert into CLIENT_RESOURCE_ACCESS values (...)
        ClientResourceAccess clientResourceAccess = new ClientResourceAccess();
        clientResourceAccess.setResourceId(existingResourceId);
        clientResourceAccess.setClientId(existingClientId);
        clientResourceAccess.setAccessCode(existingAccessCode);
        clientResourceAccess.setDescription(existingDescription);

        clientResourceAccessRepository.save(clientResourceAccess);

        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity()) // Crucial for E2E security tests
                .build();
    }


    @Test
    void checkActiveProfile() {
        // This will print the active profiles (e.g., [test]) helping you match the YAML suffix
        //System.out.println("Active profile: " + Arrays.toString(environment.getActiveProfiles()));
        String[] env = environment.getActiveProfiles();
        assertThat(Arrays.toString(env)).isEqualTo("[h2mem-test]");
    }


    @SneakyThrows
    @WithMockUser
    @Test
    void getByIds_normal() {
        // Given: by setUp
        // When & Then:: Try to fetch the user created during the setUp() phase
        mockMvc.perform(get("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}", existingClientId, existingResourceId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())                           // Expect HTTP 200 OK
                .andExpect(jsonPath("$.clientId").value(existingClientId))
                .andExpect(jsonPath("$.resourceId").value(existingResourceId))
                .andExpect(jsonPath("$.accessCode").value(existingAccessCode))
                .andExpect(jsonPath("$.description").value(existingDescription));

        // Assert Database Layer W/O MOCK: Prove the real H2 database was checked and came up with existing record
        Optional<ClientResourceAccess> databaseCheck = clientResourceAccessRepository.findByIds(existingClientId, existingResourceId);
        // This fails if the ID somehow exists, proving H2 is active and empty for these IDs
        assertTrue(databaseCheck.isPresent(), "Database should confirm this record exist!");
    }

    @SneakyThrows
    @WithMockUser
    @Test
    void getByIds_exception() {
        // Given:
        String exceptionMsg = "ClientResourceAccess not found with (clientId,resourceId): (" + missingClientId + "," + missingResourceId + ")";

        // When & Then::
        mockMvc.perform(get("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}", missingClientId, missingResourceId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())  //HTTP 404
                .andExpect(result -> {
                    Exception thrownException = result.getResolvedException();
                    assertNotNull(thrownException, "An exception should have been thrown!");
                    assertEquals(exceptionMsg, thrownException.getMessage());
                });

        // Assert Database Layer w/o MOCK: Prove the real H2 database was checked and came up empty
        Optional<ClientResourceAccess> databaseCheck = clientResourceAccessRepository.findByIds(missingClientId, missingResourceId);
        // This fails if the ID somehow exists, proving H2 is active and empty for these IDs
        assertTrue(databaseCheck.isEmpty(), "Database should confirm this record does not exist!");
    }


    @SneakyThrows
    @WithMockUser
    @Test
        //@WithMockUser(roles = "ADMIN")
        //@WithMockUser(authorities = "SCOPE_write")
    void deleteByIds_normal() {
        // Given:  setUp
        // When & Then::
        mockMvc.perform(delete("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}", existingClientId, existingResourceId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Prove that the above MOCK has deleted the existing record complete
        Optional<ClientResourceAccess> databaseCheck = clientResourceAccessRepository.findByIds(existingClientId, existingResourceId);
        assertTrue(databaseCheck.isEmpty(), "The record should have been deleted from the database!");
    }

    @SneakyThrows
    @WithMockUser
    @Test
    void deleteByNames_exception() {
        // Given:
        String exceptionMsg = "ClientResourceAccess not found with (clientId,resourceId): (" + missingClientId + "," + missingResourceId + ")";

        // When & Then:
        mockMvc.perform(delete("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}", missingClientId, missingResourceId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())  //HTTP 404
                .andExpect(result -> {
                    Exception thrownException = result.getResolvedException();
                    assertNotNull(thrownException, "An exception should have been thrown!");
                    assertEquals(exceptionMsg, thrownException.getMessage());
                });
    }


    @SneakyThrows
    @WithMockUser
    @Test
    void getAccessCodeByIds_normal() {
        // Given:
        // When & Then:
        mockMvc.perform(get("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}/access-code", existingClientId, existingResourceId))
                .andExpect(status().isOk())
                .andExpect(content().string(existingAccessCode));
    }

    @SneakyThrows
    @WithMockUser
    @Test
    void getAccessCodeByIds_exception() {
        // Given:
        String exceptionMsg = "ClientResourceAccess not found with (clientId,resourceId): (" + missingClientId + "," + missingResourceId + ")";

        // When & Then:
        mockMvc.perform(get("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}/access-code", missingClientId, missingResourceId))
                .andExpect(status().isNotFound())  //HTTP 404
                .andExpect(result -> {
                    Exception thrownException = result.getResolvedException();
                    assertNotNull(thrownException, "An exception should have been thrown!");
                    assertEquals(exceptionMsg, thrownException.getMessage());
                });
    }

    @SneakyThrows
    @WithMockUser
    @Test
    void getDescriptionByIds_normal() {
        // Given:
        // When & Then:
        mockMvc.perform(get("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}/description", existingClientId, existingResourceId))
                .andExpect(status().isOk())
                .andExpect(content().string(existingDescription));
    }

    @SneakyThrows
    @WithMockUser
    @Test
    void getDescriptionByIds_exception() {
        // Given:
        String exceptionMsg = "ClientResourceAccess not found with (clientId,resourceId): (" + missingClientId + "," + missingResourceId + ")";

        // When & Then:
        mockMvc.perform(get("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}/description", missingClientId, missingResourceId))
                .andExpect(status().isNotFound()) //HTTP 404
                .andExpect(result -> {
                    Exception thrownException = result.getResolvedException();
                    assertNotNull(thrownException, "An exception should have been thrown!");
                    assertEquals(exceptionMsg, thrownException.getMessage());
                });
    }


    @SneakyThrows
    @WithMockUser
    @Test
    void searchClientResourceAccesses_normal_access_code() {
        // Given
        // When & Then
        mockMvc.perform(get("/api/v1/client-resource-access/search")
                        .param("access-code", existingAccessCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].clientId").value(existingClientId))
                .andExpect(jsonPath("$[0].resourceId").value(existingResourceId))
                .andExpect(jsonPath("$[0].accessCode").value(existingAccessCode))
                .andExpect(jsonPath("$[0].description").value(existingDescription));
    }

    @SneakyThrows
    @WithMockUser
    @Test
    void searchClientResourceAccesses_exception_access_code() {
        // Given
        // When & Then
        mockMvc.perform(get("/api/v1/client-resource-access/search")
                        .param("access-code", missingAccessCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @SneakyThrows
    @WithMockUser
    @Test
    void searchClientResourceAccesses_normal_description() {
        // Given
        // When & Then
        mockMvc.perform(get("/api/v1/client-resource-access/search")
                        .param("description", existingDescription))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].clientId").value(existingClientId))
                .andExpect(jsonPath("$[0].resourceId").value(existingResourceId))
                .andExpect(jsonPath("$[0].accessCode").value(existingAccessCode))
                .andExpect(jsonPath("$[0].description").value(existingDescription));
    }

    @SneakyThrows
    @WithMockUser
    @Test
    void searchClientResourceAccesses_exception_description() {
        // Given
        // When & Then
        mockMvc.perform(get("/api/v1/client-resource-access/search")
                        .param("description", missingDescription))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @SneakyThrows
    @WithMockUser
    @Test
    void searchClientResourceAccesses_normal_access_code_description() {
        // Given
        // When & Then
        mockMvc.perform(get("/api/v1/client-resource-access/search")
                        .param("access-code", existingAccessCode)
                        .param("description", existingDescription))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].clientId").value(existingClientId))
                .andExpect(jsonPath("$[0].resourceId").value(existingResourceId))
                .andExpect(jsonPath("$[0].accessCode").value(existingAccessCode))
                .andExpect(jsonPath("$[0].description").value(existingDescription));
    }

    @SneakyThrows
    @WithMockUser
    @Test
    void searchClientResourceAccesses_exception_access_code_description_1() {
        // Given
        // When & Then
        mockMvc.perform(get("/api/v1/client-resource-access/search")
                        .param("access-code", missingAccessCode)
                        .param("description", existingDescription))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @SneakyThrows
    @WithMockUser
    @Test
    void searchClientResourceAccesses_exception_access_code_description_2() {
        // Given
        // When & Then
        mockMvc.perform(get("/api/v1/client-resource-access/search")
                        .param("access-code", existingAccessCode)
                        .param("description", missingDescription))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }


    @SneakyThrows
    @WithMockUser
    @Test
    void postRequest_normal() {  // post the missing record
        // Given:
        ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                .clientId(missingClientId)
                .resourceId(missingResourceId)
                .accessCode(missingAccessCode)
                .description(missingDescription)
                .build();

        String jsonPayload = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/client-resource-access")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)        // Set Content-Type: application/json
                        .content(jsonPayload))
                .andExpect(status().isCreated())                             // Expect HTTP 200 OK
                .andExpect(jsonPath("$.clientId").value(missingClientId))
                .andExpect(jsonPath("$.resourceId").value(missingResourceId))
                .andExpect(jsonPath("$.accessCode").value(missingAccessCode))
                .andExpect(jsonPath("$.description").value(missingDescription));

        // Also directly check H2 to ensure it saved successfully
        Optional<ClientResourceAccess> saved = clientResourceAccessRepository.findByIds(missingClientId, missingResourceId);
        assertTrue(saved.isPresent(), "The record should have just been saved to H2!");
    }

    @SneakyThrows
    @WithMockUser
    @Test
    void postRequest_exception() {  // post the existing record
        // Given:
        String exceptionMsg = "ClientResourceAccess already exists with (clientId,resourceId): (" +  existingClientId+ "," + existingResourceId +")";

        ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                .clientId(existingClientId)
                .resourceId(existingResourceId)
                .accessCode(existingAccessCode)
                .description(existingDescription)
                .build();

        String jsonPayload = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/api/v1/client-resource-access")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)        // Set Content-Type: application/json
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    Exception resolvedException = result.getResolvedException();
                    assertNotNull(resolvedException, "An exception should have been intercepted!");
                    assertTrue(resolvedException.getMessage().contains(exceptionMsg));
                });
    }


    @SneakyThrows
    @WithMockUser
    @Test
    void putClient_normal_put_existing() {
        // Given:
        ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                .clientId(existingResourceId)
                .resourceId(existingResourceId)
                .accessCode(missingAccessCode)
                .build();

        String jsonPayload = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(put("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}", existingClientId, existingResourceId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)        // Set Content-Type: application/json
                        .content(jsonPayload))
                .andExpect(status().isOk())                             // Expect HTTP 200 OK
                .andExpect(jsonPath("$.clientId").value(existingClientId))
                .andExpect(jsonPath("$.resourceId").value(existingResourceId))
                .andExpect(jsonPath("$.accessCode").value(missingAccessCode))
                .andExpect(jsonPath("$.description").isEmpty());

        // Verify H2 directly to ensure it saved successfully
        Optional<ClientResourceAccess> saved = clientResourceAccessRepository.findByIds(existingClientId, existingResourceId);
        assertTrue(saved.isPresent(), "The record should have just been saved to H2!");
        assertThat(saved.get().getAccessCode()).isEqualTo(request.accessCode());
        assertThat(saved.get().getDescription()).isEqualTo(request.description());
    }

    @SneakyThrows
    @WithMockUser
    @Test
    void putClient_normal_put_missing() {
        // Given:
        ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                .clientId(missingClientId)
                .resourceId(missingResourceId)
                .accessCode(existingAccessCode)
                .description(existingDescription)
                .build();

        String jsonPayload = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(put("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}", missingClientId, missingResourceId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)        // Set Content-Type: application/json
                        .content(jsonPayload))
                .andExpect(status().isOk())                             // Expect HTTP 200 OK
                .andExpect(jsonPath("$.clientId").value(missingClientId))
                .andExpect(jsonPath("$.resourceId").value(missingResourceId))
                .andExpect(jsonPath("$.accessCode").value(existingAccessCode))
                .andExpect(jsonPath("$.description").value(existingDescription));

    }


    @SneakyThrows
    @WithMockUser
    @Test
    void patchRequest_normal_patch_existing() {
        // Given:
        ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                .clientId(existingClientId)
                .resourceId(existingResourceId)
                .accessCode(missingAccessCode)
                .description(missingDescription)
                .build();

        String jsonPayload = objectMapper.writeValueAsString(request);

        // when & Then
        mockMvc.perform(patch("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}", existingClientId, existingResourceId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)        // Set Content-Type: application/json
                        .content(jsonPayload))
                .andExpect(status().isOk())                             // Expect HTTP 200 OK
                .andExpect(jsonPath("$.clientId").value(existingClientId))
                .andExpect(jsonPath("$.resourceId").value(existingResourceId))
                .andExpect(jsonPath("$.accessCode").value(missingAccessCode))
                .andExpect(jsonPath("$.description").value(missingDescription));
    }

    @SneakyThrows
    @WithMockUser
    @Test
    void patchClient_exception_patch_missing_client() {
        // Given:
        ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                .clientId(missingClientId)
                .resourceId(missingResourceId)
                .accessCode(existingAccessCode)
                .description(existingDescription)
                .build();

        String jsonPayload = objectMapper.writeValueAsString(request);

        // when & Then
        mockMvc.perform(patch("/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}", missingClientId, existingResourceId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)        // Set Content-Type: application/json
                        .content(jsonPayload))
                .andExpect(status().isNotFound());                             // Expect HTTP 200 OK
    }


    @SneakyThrows
    @WithMockUser
    @Test
    void patchClient_exception_patch_missing_resource() {
        // Given:
        ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                .clientId(missingClientId)
                .resourceId(missingResourceId)
                .accessCode(existingAccessCode)
                .description(existingDescription)
                .build();

        String jsonPayload = objectMapper.writeValueAsString(request);

        // when & Then
        mockMvc.perform(patch("/api/v1/client-resource-access/client/{id1}/resource/{id2}", existingClientId, missingResourceId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)        // Set Content-Type: application/json
                        .content(jsonPayload))
                .andExpect(status().isNotFound());                             // Expect HTTP 200 OK
    }

}