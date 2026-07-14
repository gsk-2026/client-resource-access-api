package com.dreamtech.clientresourceaccessapi.service;

import com.dreamtech.clientresourceaccessapi.dto.*;

import java.util.List;

public interface ClientService {

    ClientResponse createClient(ClientPostRequest request);

    ClientResponse replaceClient(Long id, ClientPutRequest request);

    ClientResponse updateClient(Long id, ClientPatchRequest request);

    List<ClientResponse> searchClients(String key, String description);

    void updateSecret(Long id, ClientPatchSecretRequest request);

    void deleteClientById(Long id);

    ClientResponse getClientById(Long id);

    String getKeyById(Long id);

    String getDescriptionById(Long id);

}