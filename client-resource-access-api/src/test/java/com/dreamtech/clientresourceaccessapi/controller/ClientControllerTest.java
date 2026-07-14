package com.dreamtech.clientresourceaccessapi.controller;

import com.dreamtech.clientresourceaccessapi.dto.*;
import com.dreamtech.clientresourceaccessapi.exception.ClientResourceAccessApiRuntimeException;
import com.dreamtech.clientresourceaccessapi.service.ClientService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(ClientController.class)
class ClientControllerTest {
    /*  Pure Unit Test for ClientController.
        This is Pure Unit Test for ClientController using Autowired and MockitoBean.
        Note: More test cases may be added later.   */

    @Autowired
    private MockMvc mockMvc; // Simulates HTTP requests

    @Autowired
    private ObjectMapper objectMapper; // Converts Java objects to JSON

    @MockitoBean
    private ClientService clientService; // Mocks the business logic dependency


    @Test
    void getClientById_when_client_has_all_attributes() throws Exception {
        // Given:
        Long clientId = 123456L;
        String clientKey = "Client-Key-for-123456";
        String clientDescription = "Client-Description-4-123456";
        LocalDateTime clientCreated = LocalDateTime.now();
        LocalDateTime clientUpdatedAt = LocalDateTime.now().minusMinutes(123);
        ClientResponse mockResponse = ClientResponse.builder()
                .id(clientId)
                .key(clientKey)
                .description(clientDescription)
                .createdAt(clientCreated)
                .updatedAt(clientUpdatedAt)
                .build();

        when(clientService.getClientById(clientId)).thenReturn(mockResponse);

        // When & Then:
        mockMvc.perform(get("/api/v1/client/{id}", clientId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId))
                .andExpect(jsonPath("$.key").value(clientKey))
                .andExpect(jsonPath("$.description").value(clientDescription));
    }

    @Test
    void getClientById_when_client_no_description() throws Exception {
        // Given:
        Long mockedId = 987123L;
        String mockedKey = "The-Mock-Key";
        ClientResponse mockResponse = ClientResponse.builder()
                .id(mockedId)
                .key(mockedKey)
                .build();

        when(clientService.getClientById(mockedId)).thenReturn(mockResponse);

        // When & Then:
        mockMvc.perform(get("/api/v1/client/{id}", mockedId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.key").value(mockedKey))
                .andExpect(jsonPath("$.description").doesNotExist());
    }

    @Test
    void getClientById_when_exception() throws Exception {
        // Given:
        Long missingClientId = 369L;
        String expectedMsg = "Client not found with id: " + missingClientId;
        when(clientService.getClientById(missingClientId)).thenThrow(new ClientResourceAccessApiRuntimeException(expectedMsg));

        // When & Then:
        mockMvc.perform(get("/api/v1/client/{id}", missingClientId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());  // Asserts HTTP 404
    }


    @Test
    void deleteClientById_when_normal() throws Exception {
        // Given:
        Long clientId = 852167914398L;
        Mockito.doNothing().when(clientService).deleteClientById(clientId);

        // When & Then:
        mockMvc.perform(delete("/api/v1/client/{id}", clientId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteClientById_when_throw_exception() throws Exception {
        // Given:deleteClientById
        Long missingClientId = 123L;
        doThrow(new ClientResourceAccessApiRuntimeException("Client not found with id: " + missingClientId))
                .when(clientService).deleteClientById(missingClientId);

        // When & Then:
        mockMvc.perform(delete("/api/v1/client/{id}", missingClientId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());  // HTTP 404
    }


    @Test
    void getClientKeyById_when_normal() throws Exception {
        // Given:
        Long clientId = 135L;
        String clientKey = "The-Expected-Client-Key";
        when(clientService.getKeyById(clientId)).thenReturn(clientKey);

        // When & Then:
        mockMvc.perform(get("/api/v1/client/{id}/key", clientId))
                .andExpect(status().isOk())
                .andExpect(content().string(clientKey));
    }

    @Test
    void getClientKeyById_when_exception() throws Exception {
        // Given:
        Long missingClientId = 135L;
        doThrow(new ClientResourceAccessApiRuntimeException("Client not found with id: " + missingClientId))
                .when(clientService).getKeyById(missingClientId);

        // When & Then:
        mockMvc.perform(get("/api/v1/client/{id}/key", String.valueOf(missingClientId)))
                .andExpect(status().isNotFound());  //HTTP 404
    }


    @Test
    void getClientDescriptionById_when_normal() throws Exception {
        // Given:
        Long clientId = 13579L;
        String clientDescription = "The Description for Client " + clientId;
        when(clientService.getDescriptionById(clientId)).thenReturn(clientDescription);

        // When & Then:
        mockMvc.perform(get("/api/v1/client/{id}/description", String.valueOf(clientId)))
                .andExpect(status().isOk())
                .andExpect(content().string(clientDescription));
    }

    @Test
    void getClientDescriptionById_when_exception() throws Exception {
        // Given: Mocking service behavior for NAME="135NAME"
        Long missingClientId = 13579L;
        doThrow(new ClientResourceAccessApiRuntimeException("Client not found with id: " + missingClientId))
                .when(clientService).getDescriptionById(missingClientId);

        // When & Then:
        mockMvc.perform(get("/api/v1/clienst/{id}/description", String.valueOf(missingClientId)))
                .andExpect(status().isNotFound());  //HTTP 404
    }


    @Test
    void searchClients_ByKey_two_clients() throws Exception {
        // Given
        String clientKey = "Client-ABC-Key";

        Long clientId_1 = 101L;
        String clientKey_1 = "1-"+clientKey;
        String clientDescription_1 = "Client 1 Description";

        Long clientId_2 = 221122L;
        String clientKey_2 = clientKey+"-22";

        List<ClientResponse> clientResponseList = new ArrayList<>();
        clientResponseList.add(
                ClientResponse.builder()
                        .id(clientId_1)
                        .key(clientKey_1)
                        .description(clientDescription_1)
                        .build()
        );
        clientResponseList.add(
                ClientResponse.builder()
                        .id(clientId_2)
                        .key(clientKey_2)
                        .build()
        );

        when(clientService.searchClients(clientKey, null)).thenReturn(clientResponseList);

        // When & Then
        mockMvc.perform(get("/api/v1/client/search")
                        .param("key", clientKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(clientId_1))
                .andExpect(jsonPath("$[0].key").value(clientKey_1))
                .andExpect(jsonPath("$[0].description").value(clientDescription_1))
                .andExpect(jsonPath("$[1].id").value(clientId_2))
                .andExpect(jsonPath("$[1].key").value(clientKey_2))
                .andExpect(jsonPath("$[1].description").doesNotExist());

        verify(clientService).searchClients(clientKey, null);
    }


    @Test
    void searchClients_ByDescription_when_one_clients() throws Exception {
        // Given
        String clientDescription= "Client-ABC-Description";

        Long clientId_1 = 101L;
        String clientKey_1 = "Client-1-Key";
        String clientDescription_1 = "123-" + clientDescription + "-ABC";

        List<ClientResponse> clientResponseList = new ArrayList<>();
        clientResponseList.add(
                ClientResponse.builder()
                        .id(clientId_1)
                        .key(clientKey_1)
                        .description(clientDescription_1)
                        .build()
        );

        when(clientService.searchClients(null, clientDescription)).thenReturn(clientResponseList);

        // When & Then
        mockMvc.perform(get("/api/v1/client/search")
                        .param("description", clientDescription))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(clientId_1))
                .andExpect(jsonPath("$[0].key").value(clientKey_1))
                .andExpect(jsonPath("$[0].description").value(clientDescription_1));
    }


    @Test
    void searchClients_ByKeyAndDescription_when_one_clients() throws Exception {
        // Given
        String clientKey = "Client-Key";
        String clientDescription= "Client-Description";

        Long clientId_1 = 101L;
        String clientKey_1 = "11-" + clientKey + "-22";
        String clientDescription_1 = "123-" + clientDescription + "-ABC";

        List<ClientResponse> clientResponseList = new ArrayList<>();
        clientResponseList.add(
                ClientResponse.builder()
                        .id(clientId_1)
                        .key(clientKey_1)
                        .description(clientDescription_1)
                        .build()
        );

        when(clientService.searchClients(clientKey, clientDescription)).thenReturn(clientResponseList);

        // When & Then
        mockMvc.perform(get("/api/v1/client/search")
                        .param("key", clientKey)
                        .param("description", clientDescription)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(clientId_1))
                .andExpect(jsonPath("$[0].key").value(clientKey_1))
                .andExpect(jsonPath("$[0].description").value(clientDescription_1));

        verify(clientService).searchClients(clientKey, clientDescription);
    }


    @Test
    void searchClients_ByKeyAndDescription_when_zero_clients() throws Exception {
        // Given
        String clientKey = "Client-Key";
        String clientDescription= "Client-Description";

        when(clientService.searchClients(clientKey, clientDescription)).thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(get("/api/v1/client/search")
                .param("key", clientKey)
                .param("description", clientDescription)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(clientService).searchClients(clientKey, clientDescription);
    }



    @Test
    void postRequest_when_normal() throws Exception {
        // Arrange:
        ClientPostRequest request = ClientPostRequest.builder()
                .key("My-Client-Key-123")
                //.secret("My-Top-Secret")
                .description("The Client Description is ABC")
                .build();

        String jsonPayload = objectMapper.writeValueAsString(request);

        ClientResponse mockResponse = ClientResponse.builder()
                .id(123987L)
                .key("Mocked-Response-Key")
                .description("This is the mocked DESCRIPTION")
                .build();

        when(clientService.createClient(request)).thenReturn(mockResponse);

        // Act & Assert:
        mockMvc.perform(post("/api/v1/client")
                    .contentType(MediaType.APPLICATION_JSON)        // Set Content-Type: application/json
                    .content(jsonPayload))
                .andExpect(status().isCreated())                         // Expect HTTP 200 OK
                .andExpect(jsonPath("$.id").value(mockResponse.id()))
                .andExpect(jsonPath("$.key").value(mockResponse.key()))
                .andExpect(jsonPath("$.description").value(mockResponse.description()));
    }

    @Test
    void postRequest_when_exception() throws Exception {
        // Arrange:
        String existingClientKey = "Existing-Client-Key";
        ClientPostRequest request = ClientPostRequest.builder()
                .key(existingClientKey)
                //.secret(existingClientKey + " Secret")
                .description(existingClientKey + " Description")
                .build();

        String jsonPayload = objectMapper.writeValueAsString(request);

        doThrow(new ClientResourceAccessApiRuntimeException("Client exists with key: " + existingClientKey))
                .when(clientService).createClient(any(ClientPostRequest.class));

        // Act & Assert:
        mockMvc.perform(post("/api/v1/client")
                        .contentType(MediaType.APPLICATION_JSON)    // Set Content-Type: application/json
                        .content(jsonPayload))
                        .andExpect(status().isNotFound());      // Expect HTTP 404
    }


    @Test
    void putRequest_when_with_description() throws Exception {
        // Arrange:
        Long clientId = 97531L;

        ClientPutRequest request = ClientPutRequest.builder()
                .key("Client-Key-123789")
                .description("Client Description")
                .build();

        String jsonPayload = objectMapper.writeValueAsString(request);

        ClientResponse mockResponse = ClientResponse.builder()
                .id(clientId)
                .key("Mocked-Client-Key")
                .description("Mocked Client Description")
                .build();

        when(clientService.replaceClient(any(Long.class), any(ClientPutRequest.class))).thenReturn(mockResponse);

        // Act & Assert:
        mockMvc.perform(put("/api/v1/client/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)    // Set Content-Type: application/json
                        .content(jsonPayload))
                .andExpect(status().isOk())     // Expect HTTP 200 OK
                .andExpect(jsonPath("$.id").value(mockResponse.id()))
                .andExpect(jsonPath("$.key").value(mockResponse.key()))
                .andExpect(jsonPath("$.description").value(mockResponse.description()));

        verify(clientService, times(1)).replaceClient(eq(clientId), any(ClientPutRequest.class));
    }

    @Test
    void putRequest_when_no_description() throws Exception {
        // Arrange:
        Long clientId = 987654321L;

        ClientPutRequest request = ClientPutRequest.builder()
                .key("Client-Key-123789")
                .build();

        String jsonPayload = objectMapper.writeValueAsString(request);

        ClientResponse mockResponse = ClientResponse.builder()
                .id(clientId)
                .key("Mocked-Client-Key")
                .build();

        when(clientService.replaceClient(any(Long.class), any(ClientPutRequest.class))).thenReturn(mockResponse);

        // Act & Assert:
        mockMvc.perform(put("/api/v1/client/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)    // Set Content-Type: application/json
                        .content(jsonPayload))
                .andExpect(status().isOk())     // Expect HTTP 200 OK
                .andExpect(jsonPath("$.id").value(mockResponse.id()))
                .andExpect(jsonPath("$.key").value(mockResponse.key()))
                .andExpect(jsonPath("$.description").doesNotExist());
    }

    @Test
    void putRequest_when_exception() throws Exception {
        // Given:
        Long clientId = 123456789L;

        ClientPutRequest request = ClientPutRequest.builder()
                .key("Client-123789-Key")
                .description("Client 123789 Description")
                .build();
        String jsonPayload = objectMapper.writeValueAsString(request);

        doThrow(new ClientResourceAccessApiRuntimeException("Client not found with key: The-Missing-Client-Name"))
                .when(clientService).replaceClient(any(Long.class), any(ClientPutRequest.class));

        // When & Then:
        mockMvc.perform(put("/api/v1/client/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)  // Set Content-Type: application/json
                        .content(jsonPayload))
                .andExpect(status().isNotFound()); // Expect HTTP 404
    }


    @Test
    void patchRequest_when_with_description() throws Exception {
        // Arrange:
        Long clientId = 123456789L;

        ClientPatchRequest request = ClientPatchRequest.builder()
                .key("Client-Key-ABC")
                .build();

        String jsonPayload = objectMapper.writeValueAsString(request);

        ClientResponse mockResponse = ClientResponse.builder()
                .id(clientId)
                .key("Mocked-Client-Key")
                .description("Mocked Client Description")
                .build();

        when(clientService.updateClient(any(Long.class), any(ClientPatchRequest.class))).thenReturn(mockResponse);

        // Act & Assert:
        mockMvc.perform(patch("/api/v1/client/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)    // Set Content-Type: application/json
                        .content(jsonPayload))
                .andExpect(status().isOk())     // Expect HTTP 200 OK
                .andExpect(jsonPath("$.id").value(mockResponse.id()))
                .andExpect(jsonPath("$.key").value(mockResponse.key()))
                .andExpect(jsonPath("$.description").value(mockResponse.description()));
    }

    @Test
    void patchRequest_when_no_description() throws Exception {
        // Arrange:
        Long clientId = 1357924680L;

        ClientPatchRequest request = ClientPatchRequest.builder()
                .key("Client-Key-123789")
                .build();

        String jsonPayload = objectMapper.writeValueAsString(request);

        ClientResponse mockResponse = ClientResponse.builder()
                .id(clientId)
                .key("Mocked-Client-Key")
                .build();

        when(clientService.updateClient(any(Long.class), any(ClientPatchRequest.class))).thenReturn(mockResponse);

        // Act & Assert:
        mockMvc.perform(patch("/api/v1/client/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)    // Set Content-Type: application/json
                        .content(jsonPayload))
                .andExpect(status().isOk())     // Expect HTTP 200 OK
                .andExpect(jsonPath("$.id").value(mockResponse.id()))
                .andExpect(jsonPath("$.key").value(mockResponse.key()))
                .andExpect(jsonPath("$.description").doesNotExist());
    }

    @Test
    void patchRequest_when_exception() throws Exception {
        // Given:
        Long clientId = 1357924680L;

        ClientPatchRequest request = ClientPatchRequest.builder()
                .key("Client-123789-Key")
                .build();
        String jsonPayload = objectMapper.writeValueAsString(request);

        doThrow(new ClientResourceAccessApiRuntimeException("Client not found with key: The-Missing-Client-Name"))
                .when(clientService).updateClient(any(Long.class), any(ClientPatchRequest.class));

        // When & Then:
        mockMvc.perform(patch("/api/v1/client/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)  // Set Content-Type: application/json
                        .content(jsonPayload))
                .andExpect(status().isNotFound()); // Expect HTTP 404
    }


    @Test
    void patchSecretRequest() throws Exception {
        // Arrange:
        Long clientId = 1357924680L;

        ClientPatchSecretRequest request = ClientPatchSecretRequest.builder()
                .currentSecret("The-Client-Current-Secret")
                .newSecret("The-Client-New-Secret")
                .build();

        String jsonPayload = objectMapper.writeValueAsString(request);

        ClientResponse mockResponse = ClientResponse.builder()
                .id(clientId)
                .key("Mocked-Client-Key")
                .build();

        Mockito.doNothing().when(clientService).updateSecret(clientId, request);
        when(clientService.updateClient(any(Long.class), any(ClientPatchRequest.class))).thenReturn(mockResponse);

        // Act & Assert:
        mockMvc.perform(patch("/api/v1/client/{id}/secret", clientId)
                        .contentType(MediaType.APPLICATION_JSON)    // Set Content-Type: application/json
                        .content(jsonPayload))
                .andExpect(status().isNoContent());
    }


    @Test
    void authService_A() throws Exception {
        // Arrange:
        String AuthToken = "Bearer my-secret-token";
        String clientId = "client-123";
        String header1 = "X-Extra-Header-One-Value";
        String header2 = "X-Extra-Header-Two-Value";

        // Act & Assert
        mockMvc.perform(get("/api/v1/client/auth")
                        // 1. Required specific header
                        .header("Authorization", AuthToken)
                        // 2. Optional specific header
                        .header("X-Custom-Client-Id", clientId)
                        // 3. Extra headers that will be bundled into the allHeaders Map
                        .header("X-Extra-Header-One", header1)
                        .header("X-Extra-Header-Two", header2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())              // Expect HTTP 200 OK
                .andExpect(jsonPath("$.AuthorizationToken").value(AuthToken))
                .andExpect(jsonPath("$.XCustomClientID").value(clientId))
                .andExpect(jsonPath("$.X-Extra-Header-One").value(header1))
                .andExpect(jsonPath("$.X-Extra-Header-Two").value(header2));
    }

    @Test
    void authService_B() throws Exception {
        // Arrange:
        String AuthToken = "Bearer my-secret-token";
        String header1 = "X-Extra-Header-One-Value";
        String header2 = "X-Extra-Header-Two-Value";

        // Act & Assert
        mockMvc.perform(get("/api/v1/client/auth")
                        // 1. Required specific header
                        .header("Authorization", AuthToken)
                        // 2. Optional specific header
                        //.header("X-Custom-Client-Id", clientId)
                        // 3. Extra headers that will be bundled into the allHeaders Map
                        .header("X-Extra-Header-One", header1)
                        .header("X-Extra-Header-Two", header2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())              // Expect HTTP 200 OK
                .andExpect(jsonPath("$.AuthorizationToken").value(AuthToken))
                .andExpect(jsonPath("$.XCustomClientID").doesNotExist())
                .andExpect(jsonPath("$.X-Extra-Header-One").value(header1))
                .andExpect(jsonPath("$.X-Extra-Header-Two").value(header2));
    }

}