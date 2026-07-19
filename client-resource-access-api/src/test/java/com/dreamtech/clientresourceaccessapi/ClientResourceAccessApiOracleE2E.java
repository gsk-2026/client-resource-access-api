package com.dreamtech.clientresourceaccessapi;

import com.dreamtech.clientresourceaccessapi.dto.*;
import com.dreamtech.clientresourceaccessapi.model.Client;
import com.dreamtech.clientresourceaccessapi.model.ClientResourceAccess;
import com.dreamtech.clientresourceaccessapi.model.Resource;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("oracle-test") // Loads application-oracle-test.yml
class ClientResourceAccessApiOracleE2E {
    /*  End-To-End Test:
        The real raw DIT/SIT/UAT Oracle Database is applied for the E2E Testing.
        Note: More positive/normal and negative/edge E2E scenarios may be added later.  */

    @LocalServerPort
    private int port;   // Automatically grabs the random network port assigned by Tomcat

    @Autowired private Environment environment;

    //@Autowired private TestRestTemplate restTemplate;
    private RestTemplate restTemplate;

    private String baseUrl;

    // Fields to hold fresh IDs generated for EACH test run
    private Long currentTestClientId;
    private Long currentTestResourceId;
    private Long currentTestClientResourceAccess_ClientId;
    private Long currentTestClientResourceAccess_ResourceId;


    @BeforeEach
    void setUpEach() {
        this.restTemplate = new RestTemplate(new JdkClientHttpRequestFactory());
        this.baseUrl = "http://localhost:" + port ;

        currentTestClientId = -1L;
        currentTestResourceId = -1L;
        currentTestClientResourceAccess_ClientId = -1L;
        currentTestClientResourceAccess_ResourceId = -1L;
    }

    @AfterEach
    void tearDown() {
        if (currentTestClientResourceAccess_ClientId > -1L && currentTestClientResourceAccess_ResourceId > -1L) {
            deleteClientResourceAccessByIds(currentTestClientResourceAccess_ClientId, currentTestClientResourceAccess_ResourceId);
        }
        if (currentTestResourceId > -1L) {
            deleteResourceById(currentTestResourceId);
        }
        if (currentTestClientId > -1L) {
            deleteClientById(currentTestClientId);
        }
    }


    @Test
    void checkActiveProfile() {
        // This will print the active profiles (e.g., [test]) helping you match the YAML suffix
        String[] env = environment.getActiveProfiles();
        assertThat(Arrays.toString(env)).isEqualTo("[oracle-test]");
    }


    @Test
    void getClientById_exist() {
        // Arrange: setUp
        String testKey = "getClientById_exist-Client-Key";
        String testDescription = "getClientById_exist-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        // Act:
        String url = baseUrl + "/api/v1/client/" + currentTestClientId;
        ResponseEntity<ClientResponse> response = this.restTemplate.getForEntity(url, ClientResponse.class);
        // Assert:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(currentTestClientId);
        assertThat(response.getBody().key()).isEqualTo(testKey);
        assertThat(response.getBody().description()).isEqualTo(testDescription);
    }

    @Test
    void getClientById_not_exist() {
        // Arrange: setUp
        String testKey = "getClientById_not_exist-Client-Key";
        String testDescription = "getClientById_not_exist-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        // Act:
        String url = baseUrl + "/api/v1/client/" + (currentTestClientId+1000000);
        HttpClientErrorException.NotFound exception = assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> this.restTemplate.getForEntity(url, ClientResponse.class)
        );

        // Verify the HTTP Status code is exactly 404
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        // Optional: Assert that the error response body contains specific text
        String responseBody = exception.getResponseBodyAsString();
        org.junit.jupiter.api.Assertions.assertTrue(responseBody.contains("\"error\":\"Not Found\""));
    }

    @Test
    void searchClientByKey_exist() {
        // Arrange: setUp
        String testKey = "searchClientByKey_exist-Client-Key";
        String testDescription = "searchClientByKey_exist-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        // Act:
        String url = baseUrl + "/api/v1/client/search?key={key}";
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("key", testKey);
        ResponseEntity<ClientResponse[]> response = this.restTemplate.getForEntity(url, ClientResponse[].class, urlVariables);
        // Assert:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(1);

        assertThat(response.getBody()[0].id()).isEqualTo(currentTestClientId);
        assertThat(response.getBody()[0].key()).isEqualTo(testKey);
        assertThat(response.getBody()[0].description()).isEqualTo(testDescription);
    }

    @Test
    void searchClientByKey_not_exist() {
        // Arrange: setUp
        String testKey = "searchClientByKey_not_exist-Client-Key";
        String testDescription = "searchClientByKey_not_exist-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        // Act:
        String url = baseUrl + "/api/v1/client/search?key={key}";
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("key", testKey + "-MISSINS");
        ResponseEntity<ClientResponse[]> response = this.restTemplate.getForEntity(url, ClientResponse[].class, urlVariables);
        // Assert:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(0);
    }


    @Test
    void searchClientByDescription_exist() {
        // Arrange: setUp
        String testKey = "Test-Client-Key";
        String testDescription = "searchClientByDescription_exist-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        // Act:
        String url = baseUrl + "/api/v1/client/search?description={description}";
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("description", testDescription);
        ResponseEntity<ClientResponse[]> response = this.restTemplate.getForEntity(url, ClientResponse[].class, urlVariables);
        // Assert:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(1);

        assertThat(response.getBody()[0].id()).isEqualTo(currentTestClientId);
        assertThat(response.getBody()[0].key()).isEqualTo(testKey);
        assertThat(response.getBody()[0].description()).isEqualTo(testDescription);
    }

    @Test
    void searchClientByDescription_not_exist() {
        // Arrange: setUp
        String testKey = "Test-Client-Key";
        String testDescription = "searchClientByDescription_not_exist-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        // Act:
        String url = baseUrl + "/api/v1/client/search?description={description}";
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("description", testDescription + "-MISSING");
        ResponseEntity<ClientResponse[]> response = this.restTemplate.getForEntity(url, ClientResponse[].class, urlVariables);
        // Assert:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(0);
    }

    @Test
    void searchClientByKeyAndDescription_exist() {
        // Arrange: setUp
        String testKey = "Test-Client-Key";
        String testDescription = "searchClientByKeyAndDescription_exist-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        // Act:
        String url = baseUrl + "/api/v1/client/search?key={key}&description={description}";
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("key", testKey);
        urlVariables.put("description", testDescription);
        ResponseEntity<ClientResponse[]> response = this.restTemplate.getForEntity(url, ClientResponse[].class, urlVariables);
        // Assert:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(1);

        assertThat(response.getBody()[0].id()).isEqualTo(currentTestClientId);
        assertThat(response.getBody()[0].key()).isEqualTo(testKey);
        assertThat(response.getBody()[0].description()).isEqualTo(testDescription);
    }

    @Test
    void searchClientByKeyAndDescription_not_exist_key() {
        // Arrange: setUp
        String testKey = "Test-Client-Key";
        String testDescription = "searchClientByKeyAndDescription_not_exist_key-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        // Act:
        String url = baseUrl + "/api/v1/client/search?key={key}&description={description}";
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("key", testKey + "-MISSING");
        urlVariables.put("description", testDescription);
        ResponseEntity<ClientResponse[]> response = this.restTemplate.getForEntity(url, ClientResponse[].class, urlVariables);
        // Assert:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(0);
    }

    @Test
    void searchClientByKeyAndDescription_not_exist_description() {
        // Arrange: setUp
        String testKey = "Test-Client-Key";
        String testDescription = "searchClientByKeyAndDescription_not_exist_description-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        // Act:
        String url = baseUrl + "/api/v1/client/search?key={key}&description={description}";
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("key", testKey);
        urlVariables.put("description", testDescription+"-MISSING");
        ResponseEntity<ClientResponse[]> response = this.restTemplate.getForEntity(url, ClientResponse[].class, urlVariables);
        // Assert:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(0);
    }

    @Test
    void replaceClient_existing() {
        // Arrange: setUp;
        String testKey = "Test-Client-Key";
        String testDescription = "replaceClient_existing-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        // Act
        String url = baseUrl + "/api/v1/client/" + currentTestClientId;
        ClientPutRequest request = ClientPutRequest.builder()
                    .key(testKey)
                    .description(testDescription+"-UPDATED")
                    .build();

        HttpEntity<ClientPutRequest> requestEntity = new HttpEntity<>(request);

        ResponseEntity<ClientResponse> response = this.restTemplate.exchange(
                url, HttpMethod.PUT, requestEntity, ClientResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(currentTestClientId);
        assertThat(response.getBody().key()).isEqualTo(request.key());
        assertThat(response.getBody().description()).isEqualTo(request.description());
    }

    @Test
    void replaceClient_not_existing() {
        // Arrange: setUp;
        String testKey = "Test-Client-Key";
        String testDescription = "replaceClient_not_existing-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        // Act
        String url = baseUrl + "/api/v1/client/" + (currentTestClientId+1000000);

        ClientPutRequest request = ClientPutRequest.builder()
                .key("Exist-"+testKey)
                .description(testDescription)
                .build();

        HttpEntity<ClientPutRequest> requestEntity = new HttpEntity<>(request);

        ResponseEntity<ClientResponse> response = this.restTemplate.exchange(
                url, HttpMethod.PUT, requestEntity, ClientResponse.class
        );

        Long nesClientId = currentTestClientId+1;   // id increases by 1
        deleteClientById(nesClientId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(nesClientId);
        assertThat(response.getBody().key()).isEqualTo(request.key());
        assertThat(response.getBody().description()).isEqualTo(request.description());
    }


    @Test
    void updateClient_existing() {
        // Arrange: setUp;
        String testKey = "Test-Client-Key";
        String testDescription = "updateClient_existing-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        // Act
        String url = baseUrl + "/api/v1/client/" + currentTestClientId;
        ClientPatchRequest request = ClientPatchRequest.builder()
                .key(testKey)
                .description(testDescription+"-UPDATED")
                .build();

        HttpEntity<ClientPatchRequest> requestEntity = new HttpEntity<>(request);

        ResponseEntity<ClientResponse> response = this.restTemplate.exchange(
                url, HttpMethod.PATCH, requestEntity, ClientResponse.class
        );

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().id()).isEqualTo(currentTestClientId);
        assertThat(response.getBody().key()).isEqualTo(request.key());
        assertThat(response.getBody().description()).isEqualTo(request.description());
    }

    @Test
    void updateClient_not_existing() {
        // Arrange: setUp;
        String testKey = "Test-Client-Key";
        String testDescription = "updateClient_not_existing-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        // Act
        String url = baseUrl + "/api/v1/client/" + (currentTestClientId+1000000);
        ClientPatchRequest request = ClientPatchRequest.builder()
                .key(testKey)
                .description(testDescription)
                .build();

        HttpEntity<ClientPatchRequest> requestEntity = new HttpEntity<>(request);

        HttpClientErrorException.NotFound exception = Assertions.assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> this.restTemplate.exchange(
                        url, HttpMethod.PATCH, requestEntity, ClientResponse.class
                )
        );

        // Verify the HTTP Status code is exactly 404
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        // Optional: Assert that the error response body contains specific text
        String responseBody = exception.getResponseBodyAsString();
        org.junit.jupiter.api.Assertions.assertTrue(responseBody.contains("\"error\":\"Not Found\""));
    }



    @Test
    void getResourceById_exist() {
        // Arrange: setUp
        String testKey = "Test-Resource-Key";
        String testType = "Test-Resource-Type";
        String testDescription = "getResourceById_exist-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        // Act:
        String url = baseUrl + "/api/v1/resource/" + currentTestResourceId;
        ResponseEntity<ResourceResponse> response = this.restTemplate.getForEntity(url, ResourceResponse.class);
        // Assert:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(currentTestResourceId);
        assertThat(response.getBody().key()).isEqualTo(testKey);
        assertThat(response.getBody().type()).isEqualTo(testType);
        assertThat(response.getBody().description()).isEqualTo(testDescription);
    }

    @Test
    void getResourceById_not_exist() {
        // Arrange: setUp
        String testKey = "Test-Resource-Key";
        String testType = "Test-Resource-Type";
        String testDescription = "getResourceById_not_exist-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        // Act:
        String url = baseUrl + "/api/v1/resource/" + (currentTestResourceId+1000000);
        HttpClientErrorException.NotFound exception = assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> this.restTemplate.getForEntity(url, ResourceResponse.class)
        );

        // Verify the HTTP Status code is exactly 404
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        // Optional: Assert that the error response body contains specific text
        String responseBody = exception.getResponseBodyAsString();
        org.junit.jupiter.api.Assertions.assertTrue(responseBody.contains("\"error\":\"Not Found\""));
    }

    @Test
    void searchResourceByKey_exist() {
        // Arrange: setUp
        String testKey = "Test-Resource-Key";
        String testType = "Test-Resource-Type";
        String testDescription = "searchResourceByKey_exist-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        // Act:
        String url = baseUrl + "/api/v1/resource/search?key={key}";
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("key", testKey);
        ResponseEntity<ResourceResponse[]> response = this.restTemplate.getForEntity(url, ResourceResponse[].class, urlVariables);
        // Assert:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(1);

        assertThat(response.getBody()[0].id()).isEqualTo(currentTestResourceId);
        assertThat(response.getBody()[0].key()).isEqualTo(testKey);
        assertThat(response.getBody()[0].type()).isEqualTo(testType);
        assertThat(response.getBody()[0].description()).isEqualTo(testDescription);
    }

    @Test
    void searchResourceByKey_not_exist() {
        // Arrange: setUp
        String testKey = "Test-Resource-Key";
        String testType = "Test-Resource-Type";
        String testDescription = "searchResourceByKey_not_exist-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        // Act:
        String url = baseUrl + "/api/v1/resource/search?key={key}";
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("key", testKey+"-MISSINS");
        ResponseEntity<ResourceResponse[]> response = this.restTemplate.getForEntity(url, ResourceResponse[].class, urlVariables);
        // Assert:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(0);
    }


    @Test
    void searchResourceByType_exist() {
        // Arrange: setUp
        String testKey = "Test-Resource-Key";
        String testType = "Test-Resource-Type";
        String testDescription = "searchResourceByType_exist-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        // Act:
        String url = baseUrl + "/api/v1/resource/search?type={type}";
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("type", testType);
        ResponseEntity<ResourceResponse[]> response = this.restTemplate.getForEntity(url, ResourceResponse[].class, urlVariables);
        // Assert:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(1);

        assertThat(response.getBody()[0].id()).isEqualTo(currentTestResourceId);
        assertThat(response.getBody()[0].key()).isEqualTo(testKey);
        assertThat(response.getBody()[0].type()).isEqualTo(testType);
        assertThat(response.getBody()[0].description()).isEqualTo(testDescription);
    }

    @Test
    void searchResourceByType_not_exist() {
        // Arrange: setUp
        String testKey = "Test-Resource-Key";
        String testType = "Test-Resource-Type";
        String testDescription = "searchResourceByType_not_exist-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        // Act:
        String url = baseUrl + "/api/v1/resource/search?type={type}";
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("type", testType+"-MISSINS");
        ResponseEntity<ResourceResponse[]> response = this.restTemplate.getForEntity(url, ResourceResponse[].class, urlVariables);
        // Assert:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(0);
    }

    @Test
    void searchResourceByKeyAndType_exist() {
        // Arrange: setUp
        String testKey = "Test-Resource-Key";
        String testType = "Test-Resource-Type";
        String testDescription = "searchResourceByKeyAndType_exist-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        // Act:
        String url = baseUrl + "/api/v1/resource/search?key={key}&type={type}";
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("key", testKey);
        urlVariables.put("type", testType);
        ResponseEntity<ResourceResponse[]> response = this.restTemplate.getForEntity(url, ResourceResponse[].class, urlVariables);
        // Assert:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(1);

        assertThat(response.getBody()[0].id()).isEqualTo(currentTestResourceId);
        assertThat(response.getBody()[0].key()).isEqualTo(testKey);
        assertThat(response.getBody()[0].type()).isEqualTo(testType);
        assertThat(response.getBody()[0].description()).isEqualTo(testDescription);
    }

    @Test
    void searchResourceByKeyAndType_not_exist_key() {
        // Arrange: setUp
        String testKey = "Test-Resource-Key";
        String testType = "Test-Resource-Type";
        String testDescription = "searchResourceByKeyAndType_not_exist_key-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        // Act:
        String url = baseUrl + "/api/v1/resource/search?key={key}&type={type}";
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("key", testKey+"-MISSINS");
        urlVariables.put("type", testType);
        ResponseEntity<ResourceResponse[]> response = this.restTemplate.getForEntity(url, ResourceResponse[].class, urlVariables);

        // Assert:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(0);
    }

    @Test
    void searchResourceByKeyAndType_not_exist_type() {
        // Arrange: setUp
        String testKey = "Test-Resource-Key";
        String testType = "Test-Resource-Type";
        String testDescription = "searchResourceByKeyAndType_not_exist_type-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        // Act:
        String url = baseUrl + "/api/v1/resource/search?key={key}&type={type}";
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("key", testKey);
        urlVariables.put("type", testDescription+"-MISSINS");
        ResponseEntity<ResourceResponse[]> response = this.restTemplate.getForEntity(url, ResourceResponse[].class, urlVariables);
        // Assert:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(0);
    }


    @Test
    void replaceResource_existing() {
        // Arrange: setUp;
        String testKey = "Test-Resource-Key";
        String testType = "Test-Resource-Type";
        String testDescription = "replaceResource_existing-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        // Act
        String url = baseUrl + "/api/v1/resource/" + currentTestResourceId;
        ResourcePostRequest request = ResourcePostRequest.builder()
                .key(testKey)
                .type(testDescription)
                .description("existingResourceDescription-UPDATED")
                .build();

        HttpEntity<ResourcePostRequest> requestEntity = new HttpEntity<>(request);

        ResponseEntity<ResourceResponse> response = this.restTemplate.exchange(
                url, HttpMethod.PUT, requestEntity, ResourceResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(currentTestResourceId);
        assertThat(response.getBody().key()).isEqualTo(request.key());
        assertThat(response.getBody().description()).isEqualTo(request.description());
    }

    @Test
    void replaceResource_not_existing() {
        // Arrange: setUp;
        String testKey = "Test-Resource-Key";
        String testType = "Test-Resource-Type";
        String testDescription = "replaceResource_not_existing-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        // Act
        String url = baseUrl + "/api/v1/resource/" + (currentTestResourceId+1000000);
        ResourcePostRequest request = ResourcePostRequest.builder()
                .key(testKey+"-UPDATED")
                .type("UPDATED-"+testKey)
                .description("UPDATED-"+testDescription+"-updated")
                .build();

        HttpEntity<ResourcePostRequest> requestEntity = new HttpEntity<>(request);

        ResponseEntity<ResourceResponse> response = this.restTemplate.exchange(
                url, HttpMethod.PUT, requestEntity, ResourceResponse.class
        );

        Long nextResourceId = currentTestResourceId + 1;
        deleteResourceById(nextResourceId);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(nextResourceId);
        assertThat(response.getBody().key()).isEqualTo(request.key());
        assertThat(response.getBody().description()).isEqualTo(request.description());
    }


    @Test
    void updateResource_existing() {
        // Arrange: setUp;
        String testKey = "Test-Resource-Key";
        String testType = "Test-Resource-Type";
        String testDescription = "updateResource_existing-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        // Act
        String url = baseUrl + "/api/v1/resource/" + currentTestResourceId;
        ResourcePostRequest request = ResourcePostRequest.builder()
                .key(testKey)
                .type("UPDATED-"+testType)
                .description(testDescription+"-UPDATED")
                .build();

        HttpEntity<ResourcePostRequest> requestEntity = new HttpEntity<>(request);

        ResponseEntity<ResourceResponse> response = this.restTemplate.exchange(
                url, HttpMethod.PATCH, requestEntity, ResourceResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(currentTestResourceId);
        assertThat(response.getBody().key()).isEqualTo(request.key());
        assertThat(response.getBody().description()).isEqualTo(request.description());
    }

    @Test
    void updateResource_not_existing() {
        // Arrange: setUp;
        String testKey = "Test-Resource-Key";
        String testType = "Test-Resource-Type";
        String testDescription = "updateResource_not_existing-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        // Act
        String url = baseUrl + "/api/v1/resource/" + (currentTestClientId+1000000);
        ResourcePostRequest request = ResourcePostRequest.builder()
                .key(testKey)
                .type(testType)
                .build();

        HttpEntity<ResourcePostRequest> requestEntity = new HttpEntity<>(request);

        HttpClientErrorException.NotFound exception = Assertions.assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> this.restTemplate.exchange(
                        url, HttpMethod.PATCH, requestEntity, ResourceResponse.class
                )
        );

        // Verify the HTTP Status code is exactly 404
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        // Optional: Assert that the error response body contains specific text
        String responseBody = exception.getResponseBodyAsString();
        org.junit.jupiter.api.Assertions.assertTrue(responseBody.contains("\"error\":\"Not Found\""));
    }



    @Test
    void getClientResourceAccessByIds_exist() {
        // Arrange: setUp
        String testKey = "Test-ClientResourceAccess-Key";
        String testDescription = "getClientResourceAccessByIds_exist-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        testKey = "Test-ClientResourceAccess-Key";
        String testType = "Test-ClientResourceAccess-Type";
        testDescription = "getClientResourceAccessByIds_exist-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        String testAccessCode = "Test-ClientResourceAccess-Code";
        testDescription = "getClientResourceAccessByIds_exist-ClientResourceAccess-Description";
        ClientResourceAccess access = createClientResourceAccess(currentTestClientId,
                                        currentTestResourceId, testAccessCode, testDescription);
        currentTestClientResourceAccess_ClientId = access.getClientId();
        currentTestClientResourceAccess_ResourceId = access.getResourceId();

        // Act:
        String url = baseUrl + "/api/v1/client-resource-access"
                    + "/client/" + currentTestClientResourceAccess_ClientId
                    + "/resource/" + currentTestClientResourceAccess_ResourceId;
        ResponseEntity<ClientResourceAccessResponse> response = this.restTemplate.getForEntity(url, ClientResourceAccessResponse.class);

        // Assert:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().clientId()).isEqualTo(access.getClientId());
        assertThat(response.getBody().resourceId()).isEqualTo(access.getResourceId());
        assertThat(response.getBody().accessCode()).isEqualTo(access.getAccessCode());
        assertThat(response.getBody().description()).isEqualTo(access.getDescription());
    }

    @Test
    void getClientResourceAccessByIds_not_exist() {
        // Arrange: setUp
        String testKey = "Test-ClientResourceAccess-Key";
        String testDescription = "getClientResourceAccessByIds_not_exist-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        testKey = "Test-ClientResourceAccess-Key";
        String testType = "Test-ClientResourceAccess-Type";
        testDescription = "getClientResourceAccessByIds_not_exist-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        // Act:
        String url = baseUrl + "/api/v1/client-resource-access"
                + "/client/" + currentTestClientId
                + "/resource/" + currentTestResourceId;

        HttpClientErrorException.NotFound exception = assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> this.restTemplate.getForEntity(url, ClientResourceAccessResponse.class)
        );

        // Verify the HTTP Status code is exactly 404
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        // Optional: Assert that the error response body contains specific text
        String responseBody = exception.getResponseBodyAsString();
        org.junit.jupiter.api.Assertions.assertTrue(responseBody.contains("\"error\":\"Not Found\""));
    }

    @Test
    void searchClientResourceAccessByAccessCode_exist() {
        // Arrange: setUp
        String testKey = "Test-Client-Key";
        String testDescription = "searchClientResourceAccessByAccessCode_exist-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        testKey = "Test-Resource-Key";
        String testType = "Test-Resource-Type";
        testDescription = "searchClientResourceAccessByAccessCode_exist-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        String testAccessCode = "Test-Access-Code";
        testDescription = "searchClientResourceAccessByAccessCode_exist-ClientResourceAccess-Description";
        ClientResourceAccess access = createClientResourceAccess(currentTestClientId,
                currentTestResourceId, testAccessCode, testDescription);
        currentTestClientResourceAccess_ClientId = access.getClientId();
        currentTestClientResourceAccess_ResourceId = access.getResourceId();

        // Act:
        String url = baseUrl + "/api/v1/client-resource-access/search?access-code={access-code}";
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("access-code", testAccessCode);
        ResponseEntity<ClientResourceAccessResponse[]> response = this.restTemplate.getForEntity(url, ClientResourceAccessResponse[].class, urlVariables);
        // Assert:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(1);

        assertThat(response.getBody()[0].clientId()).isEqualTo(access.getClientId());
        assertThat(response.getBody()[0].resourceId()).isEqualTo(access.getResourceId());
        assertThat(response.getBody()[0].accessCode()).isEqualTo(access.getAccessCode());
        assertThat(response.getBody()[0].description()).isEqualTo(access.getDescription());
    }

    @Test
    void searchClientResourceAccessByAccessCode_not_exist() {
        // Arrange: setUp
        // Act:
        String url = baseUrl + "/api/v1/client-resource-access/search?access-code={access-code}";
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("access-code", "searchClientResourceAccessByAccessCode_not_exist-MISSING");
        ResponseEntity<ClientResourceAccessResponse[]> response = this.restTemplate.getForEntity(url, ClientResourceAccessResponse[].class, urlVariables);
        // Assert:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(0);
    }

    @Test
    void searchClientResourceAccessByAccessCodeAndDescription_exist() {
        // Arrange: setUp
        String testKey = "Test-Client-Key";
        String testDescription = "searchClientResourceAccessByAccessCodeAndDescription_exist-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        testKey = "Test-Resource-Key";
        String testType = "Test-Resource-Type";
        testDescription = "searchClientResourceAccessByAccessCodeAndDescription_exist-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        String testAccessCode = "Test-Access-Code";
        testDescription = "searchClientResourceAccessByAccessCodeAndDescription_exist-ClientResourceAccess-Description";
        ClientResourceAccess access = createClientResourceAccess(currentTestClientId,
                currentTestResourceId, testAccessCode, testDescription);
        currentTestClientResourceAccess_ClientId = access.getClientId();
        currentTestClientResourceAccess_ResourceId = access.getResourceId();

        // Act:
        String url = baseUrl + "/api/v1/client-resource-access/search?access-code={access-code}&description={description}";
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("access-code", testAccessCode);
        urlVariables.put("description", testDescription);
        ResponseEntity<ClientResourceAccessResponse[]> response = this.restTemplate.getForEntity(url, ClientResourceAccessResponse[].class, urlVariables);
        // Assert:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(1);

        assertThat(response.getBody()[0].clientId()).isEqualTo(access.getClientId());
        assertThat(response.getBody()[0].resourceId()).isEqualTo(access.getResourceId());
        assertThat(response.getBody()[0].accessCode()).isEqualTo(access.getAccessCode());
        assertThat(response.getBody()[0].description()).isEqualTo(access.getDescription());
    }

    @Test
    void searchClientResourceAccessByAccessCodeAndDescription_not_exist_access_code() {
        // Arrange: setUp
        String formattedDate = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String testKey = "Test-Client-Key" + formattedDate;
         String testDescription = "searchClientResourceAccessByAccessCodeAndDescription_not_exist_accesscode-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        testKey = "Test-Resource-Key" + formattedDate;
        String testType = "Test-Resource-Type" + formattedDate;
        testDescription = "searchClientResourceAccessByAccessCodeAndDescription_not_exist_accesscode-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        String testAccessCode = "Test-Access-Code" + formattedDate;
        testDescription = "searchClientResourceAccessByAccessCodeAndDescription_not_exist_accesscode-ClientResourceAccess-Description";
        ClientResourceAccess access = createClientResourceAccess(currentTestClientId,
                currentTestResourceId, testAccessCode, testDescription);
        currentTestClientResourceAccess_ClientId = access.getClientId();
        currentTestClientResourceAccess_ResourceId = access.getResourceId();

        // Act:
        String url = baseUrl + "/api/v1/client-resource-access/search?access-code={access-code}&description={description}";
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("access-code", "MISSING-"+testAccessCode);
        urlVariables.put("description", testDescription);
        ResponseEntity<ClientResourceAccessResponse[]> response = this.restTemplate.getForEntity(url, ClientResourceAccessResponse[].class, urlVariables);
        // Assert:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(0);
    }

    @Test
    void searchClientResourceAccessByAccessCodeAndDescription_not_exist_description() {
        // Arrange: setUp
        String formattedDate = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String testKey = "Test-Client-Key" + formattedDate;
        String testDescription = "searchClientResourceAccessByAccessCodeAndDescription_not_exist_description-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        testKey = "Test-Resource-Key" + formattedDate;
        String testType = "Test-Resource-Type" + formattedDate;
        testDescription = "searchClientResourceAccessByAccessCodeAndDescription_not_exist_description-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        String testAccessCode = "Test-Access-Code" + formattedDate;
        testDescription = "searchClientResourceAccessByAccessCodeAndDescription_not_exist_description-ClientResourceAccess-Description";
        ClientResourceAccess access = createClientResourceAccess(currentTestClientId,
                currentTestResourceId, testAccessCode, testDescription);
        currentTestClientResourceAccess_ClientId = access.getClientId();
        currentTestClientResourceAccess_ResourceId = access.getResourceId();

        // Act:
        String url = baseUrl + "/api/v1/client-resource-access/search?access-code={access-code}&description={description}";
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("access-code", testAccessCode);
        urlVariables.put("description", testDescription+"-MISSING");
        ResponseEntity<ClientResourceAccessResponse[]> response = this.restTemplate.getForEntity(url, ClientResourceAccessResponse[].class, urlVariables);
        // Assert:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(0);
    }



    @Test
    void replaceClientResourceAccess_existing() {
        // Arrange: setUp;
        String formattedDate = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String testKey = "Test-Client-Key" + formattedDate;
        String testDescription = "replaceClientResourceAccess_existing-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        testKey = "Test-Resource-Key" + formattedDate;
        String testType = "Test-Resource-Type" + formattedDate;
        testDescription = "replaceClientResourceAccess_existing-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        String testAccessCode = "Test-Access-Code" + formattedDate;
        testDescription = "replaceClientResourceAccess_existing-ClientResourceAccess-Description";
        ClientResourceAccess access = createClientResourceAccess(currentTestClientId,
                currentTestResourceId, testAccessCode, testDescription);
        currentTestClientResourceAccess_ClientId = access.getClientId();
        currentTestClientResourceAccess_ResourceId = access.getResourceId();

        // Act
        String url = baseUrl + "/api/v1/client-resource-access"
                    + "/client/" + currentTestClientResourceAccess_ClientId
                    + "/resource/" + currentTestClientResourceAccess_ResourceId;

        ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                .accessCode(testAccessCode)
                .description(testDescription+"-UPDATED")
                .build();

        HttpEntity<ClientResourceAccessPostRequest> requestEntity = new HttpEntity<>(request);

        ResponseEntity<ClientResourceAccessResponse> response = this.restTemplate.exchange(
                url, HttpMethod.PUT, requestEntity, ClientResourceAccessResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().clientId()).isEqualTo(currentTestClientResourceAccess_ClientId);
        assertThat(response.getBody().resourceId()).isEqualTo(currentTestClientResourceAccess_ResourceId);
        assertThat(response.getBody().accessCode()).isEqualTo(request.accessCode());
        assertThat(response.getBody().description()).isEqualTo(request.description());
    }

    @Test
    void replaceClientResourceAccess_not_existing_access_code() {
        // Arrange: setUp;
        String formattedDate = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String testKey = "Test-Client-Key" + formattedDate;
        String testDescription = "replaceClientResourceAccess_not_existing_accesscose-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        testKey = "Test-Resource-Key" + formattedDate;
        String testType = "Test-Resource-Type" + formattedDate;
        testDescription = "replaceClientResourceAccess_not_existing_accesscose-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        String testAccessCode = "Test-Access-Code" + formattedDate;
        testDescription = "replaceClientResourceAccess_not_existing_accesscose-ClientResourceAccess-Description";

        // Act
        String url = baseUrl + "/api/v1/client-resource-access"
                + "/client/" + currentTestClientId
                + "/resource/" + currentTestResourceId;

        ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                .accessCode(testAccessCode)
                .description(testDescription)
                .build();

        HttpEntity<ClientResourceAccessPostRequest> requestEntity = new HttpEntity<>(request);

        ResponseEntity<ClientResourceAccessResponse> response = this.restTemplate.exchange(
                url, HttpMethod.PUT, requestEntity, ClientResourceAccessResponse.class
        );

        assertThat(response.getBody()).isNotNull();
        currentTestClientResourceAccess_ClientId = response.getBody().clientId();
        currentTestClientResourceAccess_ResourceId = response.getBody().resourceId();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().clientId()).isEqualTo(currentTestClientId);
        assertThat(response.getBody().resourceId()).isEqualTo(currentTestResourceId);
        assertThat(response.getBody().accessCode()).isEqualTo(request.accessCode());
        assertThat(response.getBody().description()).isEqualTo(request.description());
    }

    @Test
    void replaceClientResourceAccess_not_existing_description() {
        // Arrange: setUp;
        String formattedDate = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String testKey = "Test-Client-Key" + formattedDate;
        String testDescription = "replaceClientResourceAccess_not_existing_description-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        testKey = "Test-Resource-Key" + formattedDate;
        String testType = "Test-Resource-Type" + formattedDate;
        testDescription = "replaceClientResourceAccess_not_existing_description-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        String testAccessCode = "Test-Access-Code" + formattedDate;
        testDescription = "replaceClientResourceAccess_not_existing_description-ClientResourceAccess-Description";

        // Act
        String url = baseUrl + "/api/v1/client-resource-access/"
                + "/client/" + currentTestClientId
                + "/resource/" + currentTestResourceId;

        ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                .accessCode(testAccessCode)
                .description(testDescription)
                .build();

        HttpEntity<ClientResourceAccessPostRequest> requestEntity = new HttpEntity<>(request);

        ResponseEntity<ClientResourceAccessResponse> response = this.restTemplate.exchange(
                url, HttpMethod.PUT, requestEntity, ClientResourceAccessResponse.class
        );

        assertThat(response.getBody()).isNotNull();
        currentTestClientResourceAccess_ClientId = response.getBody().clientId();
        currentTestClientResourceAccess_ResourceId = response.getBody().resourceId();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().clientId()).isEqualTo(currentTestClientId);
        assertThat(response.getBody().resourceId()).isEqualTo(currentTestResourceId);
        assertThat(response.getBody().accessCode()).isEqualTo(request.accessCode());
        assertThat(response.getBody().description()).isEqualTo(request.description());
    }


    @Test
    void updateClientResourceAccess_existing() {
        // Arrange: setUp;
        String formattedDate = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String testKey = "Test-Client-Key" + formattedDate;
        String testDescription = "updateClientResourceAccess_existing-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        testKey = "Test-Resource-Key" + formattedDate;
        String testType = "Test-Resource-Type" + formattedDate;
        testDescription = "updateClientResourceAccess_existing-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        String testAccessCode = "Test-Access-Code" + formattedDate;
        testDescription = "updateClientResourceAccess_existing-ClientResourceAccess-Description";
        ClientResourceAccess access = createClientResourceAccess(currentTestClientId,
                currentTestResourceId, testAccessCode, testDescription);
        currentTestClientResourceAccess_ClientId = access.getClientId();
        currentTestClientResourceAccess_ResourceId = access.getResourceId();

        // Act
        String url = baseUrl + "/api/v1/client-resource-access"
                + "/client/" + currentTestClientResourceAccess_ClientId
                + "/resource/" + currentTestClientResourceAccess_ResourceId;

        ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                .accessCode("UPDATED-"+testAccessCode)
                .description(testDescription+"-UPDATED")
                .build();

        HttpEntity<ClientResourceAccessPostRequest> requestEntity = new HttpEntity<>(request);

        ResponseEntity<ClientResourceAccessResponse> response = this.restTemplate.exchange(
                url, HttpMethod.PATCH, requestEntity, ClientResourceAccessResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().clientId()).isEqualTo(currentTestClientId);
        assertThat(response.getBody().resourceId()).isEqualTo(currentTestResourceId);
        assertThat(response.getBody().accessCode()).isEqualTo(request.accessCode());
        assertThat(response.getBody().description()).isEqualTo(request.description());
    }

    @Test
    void updateClientResourceAccess_not_existing_client() {
        // Arrange: setUp:
        String formattedDate = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String testKey = "Test-Client-Key" + formattedDate;
        String testDescription = "updateClientResourceAccess_not_existing_client-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        testKey = "Test-Resource-Key" + formattedDate;
        String testType = "Test-Resource-Type" + formattedDate;
        testDescription = "updateClientResourceAccess_not_existing_client-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        String testAccessCode = "Test-Access-Code" + formattedDate;
        testDescription = "updateClientResourceAccess_not_existing_client-ClientResourceAccess-Description";
        ClientResourceAccess access = createClientResourceAccess(currentTestClientId,
                currentTestResourceId, testAccessCode, testDescription);
        currentTestClientResourceAccess_ClientId = access.getClientId();
        currentTestClientResourceAccess_ResourceId = access.getResourceId();

        // Act
        String url = baseUrl + "/api/v1/client-resource-access"
                + "/client/" + (currentTestClientResourceAccess_ClientId+1000000)
                + "/resource/" + currentTestClientResourceAccess_ResourceId;

        ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                .accessCode(testAccessCode+"-UPDATED")
                .description("UPDATED-"+testDescription+"-UPDATED")
                .build();

        HttpEntity<ClientResourceAccessPostRequest> requestEntity = new HttpEntity<>(request);

        HttpClientErrorException.NotFound exception = Assertions.assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> this.restTemplate.exchange(
                            url, HttpMethod.PATCH, requestEntity, ClientResourceAccessResponse.class
                    )
        );

        // Verify the HTTP Status code is exactly 404
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        // Optional: Assert that the error response body contains specific text
        String responseBody = exception.getResponseBodyAsString();
        org.junit.jupiter.api.Assertions.assertTrue(responseBody.contains("\"error\":\"Not Found\""));
    }


    @Test
    void updateClientResourceAccess_not_existing_resource() {
        // Arrange: setUp;
        String formattedDate = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String testKey = "Test-Client-Key" + formattedDate;
        String testDescription = "updateClientResourceAccess_not_existing_resource-Client-Description";
        Client client = createClient(testKey, testDescription);
        currentTestClientId = client.getId();

        testKey = "Test-Resource-Key" + formattedDate;
        String testType = "Test-Resource-Type" + formattedDate;
        testDescription = "updateClientResourceAccess_not_existing_resource-Resource-Description";
        Resource resource = createResource(testKey, testType, testDescription);
        currentTestResourceId = resource.getId();

        String testAccessCode = "Test-Access-Code" + formattedDate;
        testDescription = "updateClientResourceAccess_not_existing_resource-ClientResourceAccess-Description";
        ClientResourceAccess access = createClientResourceAccess(currentTestClientId,
                currentTestResourceId, testAccessCode, testDescription);
        currentTestClientResourceAccess_ClientId = access.getClientId();
        currentTestClientResourceAccess_ResourceId = access.getResourceId();

        // Act
        String url = baseUrl + "/api/v1/client-resource-access/"
                + "/client/" + currentTestClientResourceAccess_ClientId
                + "/resource/" + (currentTestClientResourceAccess_ResourceId+1000000);

        ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                .accessCode(testAccessCode)
                .description(testDescription)
                .build();

        HttpEntity<ClientResourceAccessPostRequest> requestEntity = new HttpEntity<>(request);

        HttpClientErrorException.NotFound exception = Assertions.assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> this.restTemplate.exchange(
                            url, HttpMethod.PATCH, requestEntity, ClientResourceAccessResponse.class
                    )
        );

        // Verify the HTTP Status code is exactly 404
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        // Optional: Assert that the error response body contains specific text
        String responseBody = exception.getResponseBodyAsString();
        org.junit.jupiter.api.Assertions.assertTrue(responseBody.contains("\"error\":\"Not Found\""));
    }



    // private
    private Client createClient(String clientKey, String clientDescription) {
        // Arrange: setUp()
        // Act
        String url = baseUrl + "/api/v1/client";
        ClientPostRequest request = ClientPostRequest.builder()
                        .key(clientKey)
                        .description(clientDescription)
                        .build();

        ResponseEntity<ClientResponse> postResponse = this.restTemplate.postForEntity(url, request, ClientResponse.class);

        // ASSERT: Verify creation
        assertThat(postResponse.getBody()).isNotNull();
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Long createdId = postResponse.getBody().id();
        String createdKey = postResponse.getBody().key();
        String createdDescription = postResponse.getBody().description();

        assertThat(createdId).isGreaterThan(0L);
        assertThat(createdKey).isEqualTo(clientKey);
        assertThat(createdDescription).isEqualTo(clientDescription);

        return Client.builder()
                .id(postResponse.getBody().id())
                .key(postResponse.getBody().key())
                .description(postResponse.getBody().description())
                .build();
    }

    private void deleteClientById(Long clientId) {
        // Arrange: setUp
        // Act
        String url = baseUrl + "/api/v1/client/" + clientId;
        ResponseEntity<ClientResponse> response = this.restTemplate.exchange(
                url, HttpMethod.DELETE, HttpEntity.EMPTY, ClientResponse.class
        );
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }


    // private
    private Resource createResource(String resourceKey, String resourceType, String resourceDescription) {
        // Arrange: setUp()
        // Act
        String url = baseUrl + "/api/v1/resource";
        ResourcePostRequest request = ResourcePostRequest.builder()
                .key(resourceKey)
                .type(resourceType)
                .description(resourceDescription)
                .build();

        ResponseEntity<ResourceResponse> response = this.restTemplate.postForEntity(url, request, ResourceResponse.class);
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isGreaterThan(0L);
        assertThat(response.getBody().key()).isEqualTo(request.key());
        assertThat(response.getBody().type()).isEqualTo(request.type());
        assertThat(response.getBody().description()).isEqualTo(request.description());

        return Resource.builder()
                .id(response.getBody().id())
                .key(response.getBody().key())
                .type(response.getBody().type())
                .type(response.getBody().description())
                .build();
    }

    private void deleteResourceById(Long resourceId) {
        // Arrange: setUp
        // Act
        String url = baseUrl + "/api/v1/resource/" + resourceId;
        ResponseEntity<ResourceResponse> response = this.restTemplate.exchange(
                url, HttpMethod.DELETE, HttpEntity.EMPTY, ResourceResponse.class
        );
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }


    // private
    private ClientResourceAccess createClientResourceAccess(Long clientId, Long resourceId, String accessCode, String description) {
        // Arrange: setUp()
        // Act
        String url = baseUrl + "/api/v1/client-resource-access";
        ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                .clientId(clientId)
                .resourceId(resourceId)
                .accessCode(accessCode)
                .description(description)
                .build();

        ResponseEntity<ClientResourceAccessResponse> response = this.restTemplate.postForEntity(url, request, ClientResourceAccessResponse.class);
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().clientId()).isEqualTo(request.clientId());
        assertThat(response.getBody().resourceId()).isEqualTo(request.resourceId());
        assertThat(response.getBody().accessCode()).isEqualTo(request.accessCode());
        assertThat(response.getBody().description()).isEqualTo(request.description());

        return ClientResourceAccess.builder()
                .clientId(response.getBody().clientId())
                .resourceId(response.getBody().resourceId())
                .accessCode(response.getBody().accessCode())
                .description(response.getBody().description())
                .build();
    }

    private void deleteClientResourceAccessByIds(Long clientId, Long resourceId) {
        // Arrange: setUp
        // Act
        String url = baseUrl + "/api/v1/client-resource-access/client/" + clientId + "/resource/" + resourceId;
        ResponseEntity<ClientResponse> response = this.restTemplate.exchange(
                url, HttpMethod.DELETE, HttpEntity.EMPTY, ClientResponse.class
        );
        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

}