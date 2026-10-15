package com.dreamtech.clientresourceaccessapi.service;

import com.dreamtech.clientresourceaccessapi.dto.ClientResourceAccessPatchRequest;
import com.dreamtech.clientresourceaccessapi.dto.ClientResourceAccessPostRequest;
import com.dreamtech.clientresourceaccessapi.dto.ClientResourceAccessPutRequest;
import com.dreamtech.clientresourceaccessapi.dto.ClientResourceAccessResponse;
import com.dreamtech.clientresourceaccessapi.exception.HttpStatus404NotFoundException;
import com.dreamtech.clientresourceaccessapi.exception.HttpStatus409AlreadyExistsException;
import com.dreamtech.clientresourceaccessapi.model.Client;
import com.dreamtech.clientresourceaccessapi.model.ClientResourceAccess;
import com.dreamtech.clientresourceaccessapi.model.Resource;
import com.dreamtech.clientresourceaccessapi.repository.ClientRepository;
import com.dreamtech.clientresourceaccessapi.repository.ClientResourceAccessRepository;

import com.dreamtech.clientresourceaccessapi.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.text.MessageFormat.format;

@Service
@RequiredArgsConstructor // Automatically injects ResourceRepository via constructor
public class ClientResourceAccessServiceImpl implements ClientResourceAccessService {

    private final ClientRepository clientRepository;
    private final ResourceRepository resourceRepository;
    private final ClientResourceAccessRepository repository;

    private final String clientMsg404 = "Client not found with id: {0}";
    private final String resourceMsg404 = "Resource not found with id: {0}";
    private final String craMsg404 = "ClientResourceAccess not found with (clientId,resourceId): ({0},{1})";

    @Override
    @Transactional
    public ClientResourceAccessResponse createClientResourceAccess(ClientResourceAccessPostRequest request) {
        Optional<Client> client = clientRepository.findById(request.clientId());
        if (client.isEmpty()) {
            throw (new HttpStatus404NotFoundException(format(clientMsg404, String.valueOf(request.clientId()))));
        }
        Optional<Resource> resource = resourceRepository.findById(request.resourceId());
        if (resource.isEmpty()) {
            throw (new HttpStatus404NotFoundException(format(resourceMsg404, String.valueOf(request.resourceId()))));
        }
        if ( repository.findByIds(request.clientId(), request.resourceId()).isPresent() ) {
            String msg409 = "ClientResourceAccess already exists with (clientId,resourceId): ({0},{1})";
            throw new HttpStatus409AlreadyExistsException(format(msg409, String.valueOf(request.clientId()), String.valueOf(request.resourceId())));
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
        Optional<Client> client = clientRepository.findById(clientId);
        if (client.isEmpty()) {
            throw (new HttpStatus404NotFoundException(format(clientMsg404, String.valueOf(clientId))));
        }
        Optional<Resource> resource = resourceRepository.findById(resourceId);
        if (resource.isEmpty()) {
            throw (new HttpStatus404NotFoundException(format(resourceMsg404, String.valueOf(resourceId))));
        }

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
        ClientResourceAccess cra = repository.findByIds(clientId, resourceId)
                .orElseThrow(() -> new HttpStatus404NotFoundException(format(craMsg404, String.valueOf(clientId), String.valueOf(resourceId))));

        Optional<Client> client = clientRepository.findById(clientId);
        if (client.isEmpty()) {
            throw (new HttpStatus404NotFoundException(format(clientMsg404, String.valueOf(clientId))));
        }
        Optional<Resource> resource = resourceRepository.findById(resourceId);
        if (resource.isEmpty()) {
            throw (new HttpStatus404NotFoundException(format(resourceMsg404, String.valueOf(resourceId))));
        }

        if (request.accessCode() != null) {
            cra.setAccessCode(request.accessCode());
        }
        if (request.description() != null) {
            cra.setDescription(request.description());
        }

        ClientResourceAccess updatedCda = repository.save(cra);
        return mapToResponse(updatedCda);
    }

    @Override
    @Transactional
    public void deleteByIds(Long clientId, Long resourceId) {
        Optional<ClientResourceAccess> optional = repository.findByIds(clientId, resourceId);
        if (optional.isEmpty()) {
            throw (new HttpStatus404NotFoundException(format(craMsg404, String.valueOf(clientId), String.valueOf(resourceId))));
        }

        repository.deleteByIds(clientId, resourceId);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResourceAccessResponse getByIds(Long clientId, Long resourceId) {
        ClientResourceAccess cr = repository.findByIds(clientId, resourceId)
                .orElseThrow(() -> new HttpStatus404NotFoundException(format(craMsg404, String.valueOf(clientId), String.valueOf(resourceId))));

        return mapToResponse(cr);
    }

    @Override
    @Transactional(readOnly = true)
    public String getAccessCodeByIds(Long clientId, Long resourceId)  {
        /*
        return repository.findAccessCodeByIds(clientId, resourceId)
            .orElseThrow(() -> new HttpStatus404NotFoundException(format(craMsg404, String.valueOf(clientId), String.valueOf(resourceId))));
         */
        ClientResourceAccess cr = repository.findByIds(clientId, resourceId)
                .orElseThrow(() -> new HttpStatus404NotFoundException(format(craMsg404, String.valueOf(clientId), String.valueOf(resourceId))));
        return cr.getAccessCode();
    }

    @Override
    @Transactional(readOnly = true)
    public String getDescriptionByIds(Long clientId, Long resourceId) {
        /*
        return repository.findDescriptionByIds(clientId, resourceId)
                .orElseThrow(() -> new HttpStatus404NotFoundException(format(craMsg404, String.valueOf(clientId), String.valueOf(resourceId))));
         */
        ClientResourceAccess cr = repository.findByIds(clientId, resourceId)
                .orElseThrow(() -> new HttpStatus404NotFoundException(format(craMsg404, String.valueOf(clientId), String.valueOf(resourceId))));
        return cr.getDescription();
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
