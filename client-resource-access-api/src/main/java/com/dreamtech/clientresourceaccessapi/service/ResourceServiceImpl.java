package com.dreamtech.clientresourceaccessapi.service;

import com.dreamtech.clientresourceaccessapi.dto.ResourcePatchRequest;
import com.dreamtech.clientresourceaccessapi.dto.ResourcePostRequest;
import com.dreamtech.clientresourceaccessapi.dto.ResourcePutRequest;
import com.dreamtech.clientresourceaccessapi.dto.ResourceResponse;
import com.dreamtech.clientresourceaccessapi.exception.ClientResourceAccessApiRuntimeException;
import com.dreamtech.clientresourceaccessapi.model.Resource;
import com.dreamtech.clientresourceaccessapi.repository.ResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.text.MessageFormat.format;

@Service 
public class ResourceServiceImpl implements ResourceService {

    @Autowired // Avoid this pattern
    private ResourceRepository resourceRepository;

    private final String msg = "Resource not found with id: {0}";

    @Override
    @Transactional
    public ResourceResponse createResource(ResourcePostRequest request) {
        List<Resource> resourceList = resourceRepository.findByKey(request.key());
        boolean hasMyKey = resourceList.stream().anyMatch(resource -> request.key().equals(resource.getKey()));

        if ((! resourceList.isEmpty()) && hasMyKey) {
            throw new IllegalArgumentException("Resource already exists with key: " + request.key());
        }

        // Map DTO to Entity
        Resource resource = Resource.builder()
                .key(request.key())
                .type(request.type())
                .description(request.description())
                .build();

        Resource savedResource = resourceRepository.save(resource);

        // Map Entity to Response DTO
        return mapToResponse(savedResource);
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceResponse getResourceById(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ClientResourceAccessApiRuntimeException(format(msg, String.valueOf(id))));

        return mapToResponse(resource);
    }

    @Override
    @Transactional
    public ResourceResponse updateResource(Long id, ResourcePatchRequest request) {
        Resource existingResource = resourceRepository.findById(id)
                .orElseThrow(() -> new ClientResourceAccessApiRuntimeException(format(msg, String.valueOf(id))));

        if (request.key() != null) {
            existingResource.setKey(request.key());
        }
        if (request.type() != null) {
            existingResource.setType(request.type());
        }
        if (request.description() != null) {
            existingResource.setDescription(request.description());
        }

        Resource updatedResource = resourceRepository.save(existingResource);
        return mapToResponse(updatedResource);
    }

    @Override
    @Transactional
    public ResourceResponse replaceResource(Long id, ResourcePutRequest request) {
        Optional<Resource> optionalResource = resourceRepository.findById(id);
        Resource resource = optionalResource.orElseGet(Resource::new);

        //resource.setId(id);   // comment out as @GeneratedValue(strategy = GenerationType.IDENTITY)
        resource.setKey(request.key());
        resource.setType(request.type());
        resource.setDescription(request.description());

        Resource replacedResource = resourceRepository.save(resource);

        return mapToResponse(replacedResource);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceResponse> searchResources(String key, String type, String description) {
        List<Resource> resourceList;

        if (key != null && type != null && description != null) {
            resourceList = resourceRepository.findByKeyAndTypeAndDescription(key, type, description);
        }
        else if (key != null && type != null) {
            resourceList = resourceRepository.findByKeyAndType(key, type);
        }
        else if (key != null && description != null) {
            resourceList = resourceRepository.findByKeyAndDescription(key, description);
        }
        else if (type != null && description != null) {
            resourceList = resourceRepository.findByTypeAndDescription(type, description);
        }
        else if (key != null) {
            resourceList = resourceRepository.findByKey(key);
        }
        else if (type != null) {
            resourceList = resourceRepository.findByType(type);
        }
        else if (description != null) {
            resourceList = resourceRepository.findByDescription(description);
        }
        else {
            resourceList = resourceRepository.findAll();
        }

        List<ResourceResponse> responseList = new ArrayList<>();

        for (Resource resource : resourceList) {
            responseList.add(mapToResponse(resource));
        }

        return responseList;
    }

    @Override
    @Transactional
    public void deleteResourceById(Long id) {
        Optional<Resource> optional = resourceRepository.findById(id);
        if (! optional.isPresent()) {
            throw (new ClientResourceAccessApiRuntimeException(format(msg, String.valueOf(id))));
        }

        resourceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public String getKeyById(Long id){
        return resourceRepository.findKeyById(id)
                .orElseThrow(() -> new ClientResourceAccessApiRuntimeException(format(msg, String.valueOf(id))));
    }

    @Override
    @Transactional(readOnly = true)
    public String getTypeById(Long id){
        return resourceRepository.findTypeById(id)
                .orElseThrow(() -> new ClientResourceAccessApiRuntimeException(format(msg, String.valueOf(id))));
    }

    @Override
    @Transactional(readOnly = true)
    public String getDescriptionById(Long id){
        return resourceRepository.findDescriptionById(id)
                .orElseThrow(() -> new ClientResourceAccessApiRuntimeException(format(msg, String.valueOf(id))));
    }


    // Helper method to convert Entity to Response DTO
    private ResourceResponse mapToResponse(Resource resource) {
        return ResourceResponse.builder()
                .id(resource.getId() != null ? resource.getId() : null)
                .key(resource.getKey() != null ? resource.getKey() : null)
                .type(resource.getType() != null ? resource.getType() : null)
                .description(resource.getDescription() != null ? resource.getDescription() : null)
                .createdAt(resource.getCreatedAt() != null ? resource.getCreatedAt() : null)
                .updatedAt(resource.getUpdatedAt() != null ? resource.getUpdatedAt() : null)
                .build();
    }

}
