package com.dreamtech.clientresourceaccessapi.service;

import com.dreamtech.clientresourceaccessapi.dto.*;
import com.dreamtech.clientresourceaccessapi.exception.ClientResourceAccessApiRuntimeException;
import com.dreamtech.clientresourceaccessapi.model.Client;
import com.dreamtech.clientresourceaccessapi.repository.ClientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.text.MessageFormat.format;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    private final PasswordEncoder passwordEncoder;

    private final String msg = "Client not found with id: {0}";

    public ClientServiceImpl(ClientRepository repository, PasswordEncoder passwordEncoder) {
        this.clientRepository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public ClientResponse createClient(ClientPostRequest request) {
        List<Client> clientList = clientRepository.findByKey(request.key());
        boolean hasMyKey = clientList.stream().anyMatch(client -> request.key().equals(client.getKey()));

        if (!clientList.isEmpty() && hasMyKey) {
            throw new IllegalArgumentException("Client already exists with key: " + request.key());
        }

        // Map DTO to Entity
        Client client = new Client();
        client.setKey(request.key());
        client.setSecretHash(passwordEncoder.encode(request.secret()));
        client.setDescription(request.description());

        Client savedClient = clientRepository.save(client);

        return mapToResponse(savedClient);
    }

    @Override
    public List<ClientResponse> searchClients(String key, String description) {
        List<Client> listClient;

        if (key != null && description != null) {
            listClient = clientRepository.findByKeyAndDescription(key, description);
        } else if (key != null) {
            listClient = clientRepository.findByKey(key);
        } else if (description != null) {
            listClient = clientRepository.findByDescription(description);
        } else {
            listClient = clientRepository.findAll();
        }

        List<ClientResponse> listResponse = new ArrayList<>();
        for (Client client : listClient) {
            listResponse.add(mapToResponse(client));
        }

        return listResponse;
    }


    @Override
    @Transactional
    public ClientResponse replaceClient(Long id, ClientPutRequest request) {
        Optional<Client> optionalClient = clientRepository.findById(id);

        Client client = optionalClient.orElseGet(Client::new);
        //client.setId(id);     // comment out as this @GeneratedValue(strategy = GenerationType.IDENTITY)
        client.setKey(request.key());
        client.setDescription(request.description());

        Client targetClient = clientRepository.save(client);

        return mapToResponse(targetClient);
    }

    @Override
    @Transactional
    public ClientResponse updateClient(Long id, ClientPatchRequest request) {
        Client existingClient = clientRepository.findById(id)
                .orElseThrow(() -> new ClientResourceAccessApiRuntimeException(format(msg, String.valueOf(id))));

        if (request.key() != null) {
            existingClient.setKey(request.key());
        }
        if (request.description() != null) {
            existingClient.setDescription(request.description());
        }

        Client updatedClient = clientRepository.save(existingClient);
        return mapToResponse(updatedClient);
    }

    @Override
    @Transactional
    public void updateSecret(Long id, ClientPatchSecretRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientResourceAccessApiRuntimeException("Client not found for id: " + id));

        String secretHash = passwordEncoder.encode(request.newSecret());    // "dummy-hash"

        client.setSecretHash(secretHash);

        clientRepository.save(client);
    }

    @Override
    @Transactional
    public void deleteClientById(Long id) {
        Optional<Client> optional = clientRepository.findById(id);
        if (! optional.isPresent()) {
            throw (new ClientResourceAccessApiRuntimeException(format(msg, String.valueOf(id))));
        }

        clientRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientResourceAccessApiRuntimeException(format(msg, String.valueOf(id))));

        return mapToResponse(client);
    }

    @Override
    @Transactional(readOnly = true)
    public String getKeyById(Long id){
        return clientRepository.findKeyById(id)
                .orElseThrow(() -> new ClientResourceAccessApiRuntimeException(format(msg, String.valueOf(id))));
    }

    @Override
    @Transactional(readOnly = true)
    public String getDescriptionById(Long id) {
        return clientRepository.findDescriptionById(id)
                .orElseThrow(() -> new ClientResourceAccessApiRuntimeException(format(msg, String.valueOf(id))));
    }


    private ClientResponse mapToResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId() != null ? client.getId() : null)
                .key(client.getKey() != null ? client.getKey() : null)
                .description(client.getDescription() != null ? client.getDescription() : null)
                .createdAt(client.getCreatedAt() != null ? client.getCreatedAt() : null)
                .updatedAt(client.getUpdatedAt() != null ? client.getUpdatedAt() : null)
                .build();
    }

}
