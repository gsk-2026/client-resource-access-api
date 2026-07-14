package com.dreamtech.clientresourceaccessapi.service;

import com.dreamtech.clientresourceaccessapi.dto.ClientResourceAccessPatchRequest;
import com.dreamtech.clientresourceaccessapi.dto.ClientResourceAccessPostRequest;
import com.dreamtech.clientresourceaccessapi.dto.ClientResourceAccessPutRequest;
import com.dreamtech.clientresourceaccessapi.dto.ClientResourceAccessResponse;
import com.dreamtech.clientresourceaccessapi.exception.ClientResourceAccessApiRuntimeException;
import com.dreamtech.clientresourceaccessapi.model.ClientResourceAccess;
import com.dreamtech.clientresourceaccessapi.repository.ClientResourceAccessRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor // Automatically injects ResourceRepository via constructor
public class ClientResourceAccessServiceImpl implements ClientResourceAccessService {

    private final ClientResourceAccessRepository repository;

    @Override
    @Transactional
    public ClientResourceAccessResponse createClientResourceAccess(ClientResourceAccessPostRequest request) {
        String exceptionMsg = "ClientResourceAccess already exists with (clientId,resourceId): (" +  request.clientId() + "," + request.resourceId() +")";

        if ( repository.findByIds(request.clientId(), request.resourceId()).isPresent() ) {
                throw new ClientResourceAccessApiRuntimeException(exceptionMsg);
        }

        ClientResourceAccess cr = ClientResourceAccess.builder()
                .clientId(request.clientId())
                .resourceId(request.resourceId())
                .accessCode(request.accessCode())
                .description(request.description())
                .build();

        ClientResourceAccess savedCR = repository.save(cr);

        return mapToResponse(savedCR);
    }

    @Override
    public List<ClientResourceAccessResponse> searchClientResourceAccesses(String accessCode, String description) {
        List<ClientResourceAccess> crList;

        if (accessCode != null && description != null) {
            crList = repository.findByAccessCodeAndDescription(accessCode, description);
        }
        else if (accessCode != null) {
            crList = repository.findByAccessCode(accessCode);
        }
        else if (description != null) {
            crList = repository.findByDescription(description);
        }
        else {
            crList = repository.findAll();
        }

        List<ClientResourceAccessResponse> responseList = new ArrayList<>();
        for (ClientResourceAccess cr : crList) {
            responseList.add(mapToResponse(cr));
        }

        return responseList;
    }

    @Override
    @Transactional
    public ClientResourceAccessResponse replaceClientResourceAccess(Long clientId, Long resourceId, ClientResourceAccessPutRequest request) {
        Optional<ClientResourceAccess> optionalCR = repository.findByIds(clientId, resourceId);

        ClientResourceAccess cr = optionalCR.orElseGet(ClientResourceAccess::new);
        cr.setClientId(clientId);
        cr.setResourceId(resourceId);
        cr.setAccessCode(request.accessCode());
        cr.setDescription(request.description());

        ClientResourceAccess targetCR = repository.save(cr);

        return mapToResponse(targetCR);
    }

    @Override
    @Transactional
    public ClientResourceAccessResponse updateClientResourceAccess(Long clientId, Long resourceId, ClientResourceAccessPatchRequest request) {
        String exceptionMsg = "ClientResourceAccess already exists with (clientId,resourceId): (" +  clientId + "," + resourceId +")";
        ClientResourceAccess cr = repository.findByIds(clientId, resourceId)
                .orElseThrow(() -> new ClientResourceAccessApiRuntimeException(exceptionMsg));

        if (request.accessCode() != null) {
            cr.setAccessCode(request.accessCode());
        }
        if (request.description() != null) {
            cr.setDescription(request.description());
        }

        ClientResourceAccess updatedCda = repository.save(cr);
        return mapToResponse(updatedCda);
    }

    @Override
    @Transactional
    public void deleteByIds(Long clientId, Long resourceId) {
        repository.findByIds(clientId, resourceId)
                .orElseThrow(() -> new ClientResourceAccessApiRuntimeException("ClientResourceAccess not found with (clientId,resourceId): (" + clientId + "," + resourceId + ")"));

        repository.deleteByIds(clientId, resourceId);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResourceAccessResponse getByIds(Long clientId, Long resourceId) {
        ClientResourceAccess cr = repository.findByIds(clientId, resourceId)
                .orElseThrow(() -> new ClientResourceAccessApiRuntimeException("ClientResourceAccess not found with (clientId,resourceId): (" + clientId + "," + resourceId + ")"));

        return mapToResponse(cr);
    }

    @Override
    @Transactional(readOnly = true)
    public String getAccessCodeByIds(Long clientId, Long resourceId)  {
        return repository.findAccessCodeByIds(clientId, resourceId)
            .orElseThrow(() -> new ClientResourceAccessApiRuntimeException("ClientResourceAccess not found with (clientId,resourceId): (" + clientId + "," + resourceId + ")"));
    }

    @Override
    @Transactional(readOnly = true)
    public String getDescriptionByIds(Long clientId, Long resourceId) {
        return repository.findDescriptionByIds(clientId, resourceId)
                .orElseThrow(() -> new ClientResourceAccessApiRuntimeException("ClientResourceAccess not found with (clientId,resourceId): (" + clientId + "," + resourceId + ")"));
    }


    // Helper method to convert Entity to Response DTO
    private ClientResourceAccessResponse mapToResponse(ClientResourceAccess cr) {
        return ClientResourceAccessResponse.builder()
                .clientId(cr.getClientId() != null ? cr.getClientId() : null)
                .resourceId(cr.getResourceId() != null ? cr.getResourceId() : null)
                .accessCode(cr.getAccessCode() != null ? cr.getAccessCode() : null)
                .description(cr.getDescription() != null ? cr.getDescription() : null)
                .build();
    }

}
