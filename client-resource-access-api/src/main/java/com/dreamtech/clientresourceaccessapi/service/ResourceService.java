package com.dreamtech.clientresourceaccessapi.service;

import com.dreamtech.clientresourceaccessapi.dto.ResourcePatchRequest;
import com.dreamtech.clientresourceaccessapi.dto.ResourcePostRequest;
import com.dreamtech.clientresourceaccessapi.dto.ResourcePutRequest;
import com.dreamtech.clientresourceaccessapi.dto.ResourceResponse;

import java.util.List;

public interface ResourceService {

    ResourceResponse createResource(ResourcePostRequest request);

    ResourceResponse getResourceById(Long id);

    ResourceResponse updateResource(Long id, ResourcePatchRequest request);

    ResourceResponse replaceResource(Long id, ResourcePutRequest request);

    List<ResourceResponse> searchResources(String key, String type, String description);

    void deleteResourceById(Long id);

    String getKeyById(Long id);

    String getTypeById(Long id);

    String getDescriptionById(Long id);

}
