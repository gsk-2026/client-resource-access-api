package com.dreamtech.clientresourceaccessapi.service;

import com.dreamtech.clientresourceaccessapi.dto.*;
import com.dreamtech.clientresourceaccessapi.exception.ClientResourceAccessApiRuntimeException;
import com.dreamtech.clientresourceaccessapi.model.Client;
import com.dreamtech.clientresourceaccessapi.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test") // Activates application-test.yml
class ClientServiceImplTest {
    /*  Pure Unit Test.
        Unit Test for ClientServiceImpl using Mock & InjectMocks.
        Note: More test cases may be added later.   */

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ClientRepository clientRepository; // Mock the dependency used by the implementation

    @InjectMocks
    private ClientServiceImpl clientService; // Declare the exact implementation class here

    @Autowired
    private Environment environment;


    @Test
    void testActiveProfile() {
        String[] env = environment.getActiveProfiles();
        assertThat(Arrays.toString(env)).isEqualTo("[test]");
    }

    @Test
    void createClient_success() {
        // GIVEN
        Client mockClient = new Client();
        mockClient.setId(123789L);
        mockClient.setKey("Client-Key");
        mockClient.setDescription("CLIENT DESCRIPTION");

        ClientPostRequest request = ClientPostRequest.builder()
                .key("My-Client_Key")
                .description("My Client Description")
                .build();

        when(clientRepository.findByKey(any(String.class))).thenReturn(List.of());
        when(clientRepository.save(any(Client.class))).thenReturn(mockClient);

        // WHEN & THEN
        ClientResponse response = clientService.createClient(request);
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(mockClient.getId());
        assertThat(response.key()).isEqualTo(mockClient.getKey());
        assertThat(response.description()).isEqualTo(mockClient.getDescription());
    }

    @Test
    void createClient_exception() {
        // GIVEN
        String existingClientKey = "Client-Key";
        String exceptionMsg = "Client already exists with key: " + existingClientKey;
        Client client = new Client();
        client.setKey(existingClientKey);

        ClientPostRequest request = ClientPostRequest.builder().key(existingClientKey).build();

        when(clientRepository.findByKey(existingClientKey)).thenReturn(List.of(client));

        // WHEN & THEN
        assertThatThrownBy(() -> clientService.createClient(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(exceptionMsg);

        // Verify that save was never called because it threw an exception early
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void searchClients_ByKey() {
        // GIVEN
        String clientKey = "Client-ABC-Key";

        Client client = new Client();
        client.setKey("Mocked-Client-Key");
        client.setDescription("Mocked Client Description");

        when(clientRepository.findByKey(clientKey)).thenReturn(List.of(client));

        // WHEN & THEN
        List<ClientResponse> responseList = clientService.searchClients(clientKey, null);

        assertThat(responseList.size()).isEqualTo(1);
        assertThat(responseList.getFirst().key()).isEqualTo(client.getKey());
        assertThat(responseList.getFirst().description()).isEqualTo(client.getDescription());
    }


    @Test
    void searchClients_ByDescription() {
        // GIVEN
        String clientDescription = "Client ABC description";

        Client client = new Client();
        client.setKey("Mocked-Client-Key-ABC");
        client.setDescription("Mocked Client Description 123");

        when(clientRepository.findByDescription(clientDescription)).thenReturn(List.of(client));

        // WHEN & THEN
        List<ClientResponse> responseList = clientService.searchClients(null, clientDescription);

        assertThat(responseList.size()).isEqualTo(1);
        assertThat(responseList.getFirst().key()).isEqualTo(client.getKey());
    }

    @Test
    void searchClients_ByKeyAndDescription() {
        // GIVEN
        String clientKey = "Client-ABC-Key";
        String clientDescription = "Client ABC description";

        Client client = new Client();
        client.setKey("123-Mocked-Client-Key-ABC");
        client.setDescription("123 Mocked Client Description 123");

        when(clientRepository.findByKeyAndDescription(clientKey, clientDescription)).thenReturn(List.of(client));

        // WHEN & THEN
        List<ClientResponse> responseList = clientService.searchClients(clientKey, clientDescription);

        assertThat(responseList.size()).isEqualTo(1);
        assertThat(responseList.getFirst().key()).isEqualTo(client.getKey());
    }

    @Test
    void searchClients_ByAll() {
        // GIVEN
        Client client_1 = new Client();
        client_1.setId(111111L);
        client_1.setKey("1-Mocked-Client-Key-A");
        client_1.setDescription("1 Mocked Client Description 1");

        Client client_2 = new Client();
        client_2.setId(22222222L);
        client_2.setKey("22-Mocked-Client-Key-BB");

        when(clientRepository.findAll()).thenReturn(List.of(client_1, client_2));

        // WHEN & THEN
        List<ClientResponse> responseList = clientService.searchClients(null, null);

        assertThat(responseList.size()).isEqualTo(2);

        assertThat(responseList.getFirst().id()).isEqualTo(client_1.getId());
        assertThat(responseList.getFirst().key()).isEqualTo(client_1.getKey());
        assertThat(responseList.getFirst().description()).isEqualTo(client_1.getDescription());

        assertThat(responseList.get(1).id()).isEqualTo(client_2.getId());
        assertThat(responseList.get(1).key()).isEqualTo(client_2.getKey());
        assertThat(responseList.get(1).description()).isNull();
    }


    @Test
    void replaceClient_save_as_new() {
        // GIVEN
        Long clientId = 789123L;
        String clientKey = "Client-789123-Key";

        Client mockClient = new Client();
        mockClient.setId(clientId);
        mockClient.setKey(clientKey);
        mockClient.setDescription("CLIENT DESCRIPTION for Id: " + clientId + ", Key: " + clientKey);

        ClientPutRequest request = ClientPutRequest.builder()
                .key(mockClient.getKey())
                .description(mockClient.getDescription())
                .build();

        when(clientRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        when(clientRepository.save(any(Client.class))).thenReturn(mockClient);

        // WHEN & THEN
        ClientResponse clientResponse = clientService.replaceClient(clientId, request);
        assertThat(clientResponse).isNotNull();
        assertThat(clientResponse.id()).isEqualTo(mockClient.getId());
        assertThat(clientResponse.key()).isEqualTo(mockClient.getKey());
        assertThat(clientResponse.description()).isEqualTo(mockClient.getDescription());

        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void replaceClient_for_existing_client() {
        // GIVEN
        Long clientId = 789123L;
        String clientKey = "Client-789123-Key";

        Client client = new Client();
        client.setId(clientId);
        client.setKey(clientKey);

        Client mockClient = new Client();
        mockClient.setId(clientId);
        mockClient.setKey(clientKey);
        mockClient.setDescription("CLIENT DESCRIPTION for Id: " + clientId + ", Key: " + clientKey);

        ClientPutRequest request = ClientPutRequest.builder()
                .key(mockClient.getKey())
                .description(mockClient.getDescription())
                .build();

        when(clientRepository.findById(any(Long.class))).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(mockClient);

        // WHEN & THEN
        ClientResponse clientResponse = clientService.replaceClient(clientId, request);
        assertThat(clientResponse).isNotNull();
        assertThat(clientResponse.id()).isEqualTo(mockClient.getId());
        assertThat(clientResponse.key()).isEqualTo(mockClient.getKey());
        assertThat(clientResponse.description()).isEqualTo(mockClient.getDescription());

        verify(clientRepository, times(1)).save(any(Client.class));
    }


    @Test
    void updateClient_with_key_description() {
        // GIVEN
        Long clientId = 789123L;
        Client mockClient = new Client();
        mockClient.setId(clientId);
        mockClient.setKey("Client-Key-" + clientId);
        mockClient.setDescription("CLIENT DESCRIPTION for Id: " + clientId);

        ClientPatchRequest request = ClientPatchRequest.builder()
                .key(mockClient.getKey())
                .description(mockClient.getDescription())
                .build();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(new Client()));
        when(clientRepository.save(any(Client.class))).thenReturn(mockClient);

        // WHEN & THEN
        ClientResponse clientResponse = clientService.updateClient(clientId, request);
        assertThat(clientResponse).isNotNull();
        assertThat(clientResponse.id()).isEqualTo(mockClient.getId());
        assertThat(clientResponse.key()).isEqualTo(mockClient.getKey());
        assertThat(clientResponse.description()).isEqualTo(mockClient.getDescription());

        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void updateClient_with_only_description() {
        // GIVEN
        Long clientId = 789123L;
        Client mockClient = new Client();
        mockClient.setId(clientId);
        mockClient.setDescription("THIS IS THE CLIENT DESCRIPTION for Id: " + clientId);

        ClientPatchRequest request = ClientPatchRequest.builder()
                .key(mockClient.getKey())
                .description(mockClient.getDescription())
                .build();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(new Client()));
        when(clientRepository.save(any(Client.class))).thenReturn(mockClient);

        // WHEN & THEN
        ClientResponse clientResponse = clientService.updateClient(clientId, request);
        assertThat(clientResponse).isNotNull();
        assertThat(clientResponse.id()).isEqualTo(mockClient.getId());
        assertThat(clientResponse.key()).isNull();
        assertThat(clientResponse.description()).isEqualTo(mockClient.getDescription());

        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void updateClient_exception() {
        // GIVEN
        Long clientId = 987654321L;
        String exceptionMsg = "Client not found with id: " + clientId;

        //doThrow(new ClientResourceAccessApiRuntimeException(exceptionMsg)).when(clientRepository).findById(clientId);
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        ClientPatchRequest request = ClientPatchRequest.builder().key("Client-Key").build();

        // WHEN & THEN
        assertThatThrownBy(() -> clientService.updateClient(clientId, request))
                .isInstanceOf(ClientResourceAccessApiRuntimeException.class)
                .hasMessageContaining(exceptionMsg);

        // Verify
        verify(clientRepository, never()).save(any(Client.class));
    }


    @Test
    void updateSecret_normal() {
        // GIVEN
        Long clientId = 789123L;

        Client client = new Client();
        client.setId(clientId);
        client.setDescription("CLIENT DESCRIPTION for Id: " + clientId);

        Client mockClient = new Client();
        mockClient.setId(clientId);
        mockClient.setDescription("MOCKED CLIENT DESCRIPTION for Id: " + clientId);

        ClientPatchSecretRequest request = ClientPatchSecretRequest.builder()
                .currentSecret("My-Current-Top-Secret-"+clientId)
                .newSecret("My-New-Top-Secret-"+clientId)
                .build();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(passwordEncoder.encode(anyString())).thenReturn("the-server-encoded-secret-for-client-id-"+clientId);
        when(clientRepository.save(any(Client.class))).thenReturn(mockClient);

        // WHEN & THEN
        clientService.updateSecret(clientId, request);

        // Verify
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void updateSecret_exception() {
        // GIVEN
        Long clientId = 789123L;
        String exceptionMsg = "Client not found for id: " + clientId;

        ClientPatchSecretRequest request = ClientPatchSecretRequest.builder()
                .currentSecret("My-Current-Top-Secret-"+clientId)
                .newSecret("My-New-Top-Secret-"+clientId)
                .build();

        //doThrow(new ClientResourceAccessApiRuntimeException(exceptionMsg)).when(clientRepository).findById(clientId);
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> clientService.updateSecret(clientId, request))
                .isInstanceOf(ClientResourceAccessApiRuntimeException.class)
                .hasMessageContaining(exceptionMsg);

        // Verify
        verify(clientRepository, never()).save(any(Client.class));
    }


    @Test
    void deleteClientById_normal() {
        // GIVEN
        Long clientId = 97531L;

        when(clientRepository.findById((clientId))).thenReturn(Optional.of(new Client()));
        doNothing().when(clientRepository).deleteById(clientId);

        // WHEN & THEN
        clientService.deleteClientById(clientId);

        // Verify that delete was called exactly once with the correct client object
        verify(clientRepository, times(1)).findById(clientId);
        verify(clientRepository, times(1)).deleteById(clientId);
    }

    @Test
    void deleteClientById_Exception() {
        // GIVEN
        Long clientId = 97531L;
        String exceptionMsg = "Client not found with id: " + clientId;

        //doThrow(new ClientResourceAccessApiRuntimeException(exceptionMsg)).when(clientRepository).findById(clientId);
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> clientService.deleteClientById(clientId))
                .isInstanceOf(ClientResourceAccessApiRuntimeException.class)
                .hasMessageContaining(exceptionMsg);

        // Verify
        verify(clientRepository, never()).deleteById(anyLong());
    }


    @Test
    void getClientById_normal() {
        // GIVEN
        Long clientId = 13579L;

        Client client = new Client();
        client.setId(clientId);
        client.setKey("Client-Key-"+clientId);
        client.setDescription("Client-Description-"+clientId);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        // WHEN & THEN
        ClientResponse response = clientService.getClientById(clientId);

        assertThat(response.id()).isEqualTo(client.getId());
        assertThat(response.key()).isEqualTo(client.getKey());

    }

    @Test
    void getClientById_exception() {
        // GIVEN
        Long clientId = 13579L;
        String exceptionMsg = "Client not found with id: " + clientId;

        //doThrow(new ClientResourceAccessApiRuntimeException(exceptionMsg)).when(clientRepository).findById(clientId);
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> clientService.getClientById(clientId))
                .isInstanceOf(ClientResourceAccessApiRuntimeException.class)
                .hasMessageContaining(exceptionMsg);
    }


    @Test
    void getKeyById_normal() {
        // GIVEN
        Long clientId = 13579L;
        String mockedKey = "Mocked-Client-Key";

        when(clientRepository.findKeyById(clientId)).thenReturn(Optional.of(mockedKey));

        // WHEN & THEN
        String key = clientService.getKeyById(clientId);

        assertThat(key).isEqualTo(mockedKey);
    }

    @Test
    void getKeyById_exception() {
        // GIVEN
        Long clientId = 13579L;
        String exceptionMsg = "Client not found with id: " + clientId;

        when(clientRepository.findKeyById(clientId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> clientService.getKeyById(clientId))
                .isInstanceOf(ClientResourceAccessApiRuntimeException.class)
                .hasMessageContaining(exceptionMsg);
    }


    @Test
    void getDescriptionById_normal() {
        // GIVEN
        Long clientId = 13579L;
        String mockedDescription = "Mocked-Client-Key";

        when(clientRepository.findDescriptionById(clientId)).thenReturn(Optional.of(mockedDescription));

        // WHEN & THEN
        String description = clientService.getDescriptionById(clientId);

        assertThat(description).isEqualTo(mockedDescription);
    }

    @Test
    void getDescriptionById_exception() {
        // GIVEN
        Long clientId = 13579L;
        String exceptionMsg = "Client not found with id: " + clientId;

        when(clientRepository.findDescriptionById(clientId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> clientService.getDescriptionById(clientId))
                .isInstanceOf(ClientResourceAccessApiRuntimeException.class)
                .hasMessageContaining(exceptionMsg);
    }

}