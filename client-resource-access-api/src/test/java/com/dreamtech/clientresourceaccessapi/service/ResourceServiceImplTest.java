package com.dreamtech.clientresourceaccessapi.service;

import com.dreamtech.clientresourceaccessapi.dto.*;
import com.dreamtech.clientresourceaccessapi.exception.ClientResourceAccessApiRuntimeException;
import com.dreamtech.clientresourceaccessapi.model.Resource;
import com.dreamtech.clientresourceaccessapi.repository.ResourceRepository;
import groovy.util.ResourceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceServiceImplTest {
    /*  Pure Unit Test.
        Unit Test for ResourceServiceImpl using Mock & InjectMocks.  */

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ResourceServiceImpl resourceService;


    @Test
    void createResource_success() {
        ResourcePostRequest request = ResourcePostRequest.builder()
                .key("RESOURCE-KEY")
                .type("RESOURCE-TYPE")
                .description("RESOURCE-DESCRIPTION")
                .build();

        when(resourceRepository.findByKey("RESOURCE-KEY")).thenReturn(List.of());

        Resource saved =
                mockedResource(
                        1L,
                        "Saved-RESOURCE-KEY",
                        "Saved-RESOURCE-TYPE",
                        "Saved-RESOURCE-DESCRIPTION");

        when(resourceRepository.save(any(Resource.class))).thenReturn(saved);

        ResourceResponse response = resourceService.createResource(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(saved.getKey(), response.key());
        assertEquals(saved.getType(), response.type());
        assertEquals(saved.getDescription(), response.description());

        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    void createResource_duplicateKey() {
        Resource existing =  mockedResource(
                        10L,
                        "KEY",
                        "TYPE",
                        "Existing - DESCRIPTION");

        when(resourceRepository.findByKey("KEY")).thenReturn(List.of(existing));

        ResourcePostRequest request = ResourcePostRequest.builder()
                .key("KEY")
                    .type("TYPE")
                        .description("New - DESCRIPTION")
                            .build();

        assertThrows(IllegalArgumentException.class, () -> resourceService.createResource(request));

        verify(resourceRepository, never()).save(any());
    }



    @Test
    void getResourceById_success() {
        Resource resource = mockedResource(
                        123L,
                        "My-Key",
                        "My-Type",
                        "My dsecription");

        when(resourceRepository.findById(123L)).thenReturn(Optional.of(resource));

        ResourceResponse response = resourceService.getResourceById(123L);

        assertNotNull(response);
        assertEquals(123L, response.id());
        assertEquals(resource.getKey(), response.key());
        assertEquals(resource.getType(), response.type());
        assertEquals(resource.getDescription(), response.description());
    }

    @Test
    void getResourceById_notFound() {
        when(resourceRepository.findById(987L)).thenReturn(Optional.empty());

        assertThrows(ClientResourceAccessApiRuntimeException.class, () -> resourceService.getResourceById(987L));
    }



    @Test
    void updateResource_keyOnly() {
        Resource resource = mockedResource(
                135L,
                "App-Key",
                "App-Type",
                "Old App Description");

        when(resourceRepository.findById(135L)).thenReturn(Optional.of(resource));

        when(resourceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ResourcePatchRequest request = ResourcePatchRequest.builder()
                .key("New-Key")
                .type(null)
                .description(null)
                .build();

        ResourceResponse response = resourceService.updateResource(135L, request);

        assertNotNull(response);
        assertEquals(resource.getId(), response.id());
        assertEquals(request.key(), response.key());
        assertEquals(resource.getType(), response.type());
        assertEquals(resource.getDescription(), response.description());
    }

    @Test
    void updateResource_typeOnly() {
        Resource resource = mockedResource(
                135L,
                "App-Key",
                "App-Type",
                "Old App Description");

        when(resourceRepository.findById(135L)).thenReturn(Optional.of(resource));

        when(resourceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ResourcePatchRequest request = ResourcePatchRequest.builder()
                .key(null)
                .type("New-Type")
                .description(null)
                .build();

        ResourceResponse response = resourceService.updateResource(135L, request);

        assertNotNull(response);
        assertEquals(resource.getId(), response.id());
        assertEquals(resource.getKey(), response.key());
        assertEquals(request.type(), response.type());
        assertEquals(resource.getDescription(), response.description());
    }

    @Test
    void updateResource_descriptionOnly() {
        Resource resource = mockedResource(
                        135L,
                        "App-Key",
                        "App-Type",
                        "Old App Description");

        when(resourceRepository.findById(135L)).thenReturn(Optional.of(resource));

        when(resourceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ResourcePatchRequest request = ResourcePatchRequest.builder()
                .key(null)
                .type(null)
                .description("New App Description")
                .build();

        ResourceResponse response = resourceService.updateResource(135L, request);

        assertNotNull(response);
        assertEquals(resource.getId(), response.id());
        assertEquals(resource.getKey(), response.key());
        assertEquals(resource.getType(), response.type());
        assertEquals(request.description(), response.description());
    }

    @Test
    void updateResource_key_type_description_All() {
        Resource resource = mockedResource(
                135L,
                "App-Key",
                "App-Type",
                "Old App Description");

        when(resourceRepository.findById(135L)).thenReturn(Optional.of(resource));

        when(resourceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ResourcePatchRequest request = ResourcePatchRequest.builder()
                .key("New-Key")
                .type("New-Type")
                .description("New App Description")
                .build();

        ResourceResponse response = resourceService.updateResource(135L, request);

        assertNotNull(response);
        assertEquals(resource.getId(), response.id());
        assertEquals(request.key(), response.key());
        assertEquals(request.type(), response.type());
        assertEquals(request.description(), response.description());
    }


    @Test
    void replaceResource_existing() {
        Resource resource = mockedResource(123L,
                "old-key",
                "old-type",
                null);
        Resource mockResource = mockedResource(456L,
                "new-key",
                "new-type",
                "new--description");

        ResourcePutRequest request = ResourcePutRequest.builder()
                .key(mockResource.getKey())
                .type(mockResource.getType())
                .description(mockResource.getDescription())
                .build();

        when(resourceRepository.findById(any(Long.class))).thenReturn(Optional.of(resource));
        when(resourceRepository.save(any(Resource.class))).thenReturn(mockResource);

        // WHEN & THEN
        ResourceResponse response = resourceService.replaceResource(123L, request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(mockResource.getId());
        assertThat(response.key()).isEqualTo(mockResource.getKey());
        assertThat(response.description()).isEqualTo(mockResource.getDescription());

        verify(resourceRepository, times(1)).save(any(Resource.class));
    }

    @Test
    void replaceResource_new_resource() {
        Resource mockResource = mockedResource(369L,
                "new-key",
                "new-type",
                "new--description");

        ResourcePutRequest request = ResourcePutRequest.builder()
                .key(mockResource.getKey())
                .type(mockResource.getType())
                .description(mockResource.getDescription())
                .build();

        when(resourceRepository.findById(any(Long.class))).thenReturn(Optional.empty());
        when(resourceRepository.save(any(Resource.class))).thenReturn(mockResource);

        // WHEN & THEN
        ResourceResponse response = resourceService.replaceResource(987L, request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(mockResource.getId());
        assertThat(response.key()).isEqualTo(mockResource.getKey());
        assertThat(response.description()).isEqualTo(mockResource.getDescription());

        verify(resourceRepository, times(1)).save(any(Resource.class));
    }



    @Test
    void searchResources() {
        //TODO
    }



    @Test
    void deleteResourceById_success() {
        Resource resource = mockedResource(
                        357L,
                        "357-Key",
                        "357-Type",
                        "357 description");

        when(resourceRepository.findById(357L)).thenReturn(Optional.of(resource));

        resourceService.deleteResourceById(357L);

        verify(resourceRepository).deleteById(357L);
    }

    @Test
    void deleteResourceById_notFound() {
        when(resourceRepository.findById(333L)).thenReturn(Optional.empty());

        assertThrows(ClientResourceAccessApiRuntimeException.class, () -> resourceService.deleteResourceById(333L));

        verify(resourceRepository, never()).deleteById(anyLong());
    }


    @Test
    void getKeyById_success() {
        when(resourceRepository.findKeyById(1L)).thenReturn(Optional.of("Returned-Key"));

        assertEquals("Returned-Key", resourceService.getKeyById(1L));
    }

    @Test
    void getTypeById_notFound() {
        when(resourceRepository.findKeyById(1L)).thenReturn(Optional.empty());

        assertThrows(ClientResourceAccessApiRuntimeException.class, () -> resourceService.getKeyById(1L));
    }

    @Test
    void getDescriptionById_success() {
        when(resourceRepository.findDescriptionById(1L)).thenReturn(Optional.of("Returned Description"));

        assertEquals("Returned Description", resourceService.getDescriptionById(1L));
    }

    @Test
    void getDescriptionById_notFound() {
        when(resourceRepository.findDescriptionById(1L)).thenReturn(Optional.empty());

        assertThrows(ClientResourceAccessApiRuntimeException.class, () -> resourceService.getDescriptionById(1L));
    }

    

    private Resource mockedResource(
            Long resourceId,
            String resourceKey,
            String resourceType,
            String resourceDescription) {

        return Resource.builder()
                .id(resourceId)
                .key(resourceKey)
                .type(resourceType)
                .description(resourceDescription)
                .build();
    }
}