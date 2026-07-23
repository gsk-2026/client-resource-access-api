package com.dreamtech.clientresourceaccessapi.controller;

import com.dreamtech.clientresourceaccessapi.dto.*;
import com.dreamtech.clientresourceaccessapi.exception.ClientResourceAccessApiRuntimeException;
import com.dreamtech.clientresourceaccessapi.service.ResourceService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import static org.mockito.ArgumentMatchers.any;


@WebMvcTest(ResourceController.class)
@ActiveProfiles("test") // Activates application-test.yml
class ResourceControllerTest {
    /*  Pure unit Test for ResourceControllerTest
        This is Pure Unit Test for ResourceControllerTest using Autowired and MockitoBean.
        Note: More test cases may be added later. */

    @Autowired MockMvc mockMvc;

    @Autowired ObjectMapper objectMapper;

    @MockitoBean ResourceService resourceService;

    @Autowired
    private Environment environment;


    @Test
    void testActiveProfile() {
        String[] env = environment.getActiveProfiles();
        assertThat(Arrays.toString(env)).isEqualTo("[test]");
    }


    @Test
    void getResourceById_success() throws Exception {
        ResourceResponse response = mockedResponse(123L,
                "Resource-123-Key",
                "Resource-123-Type",
                "Resource 123 Description");

        when(resourceService.getResourceById(123L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/resource/123").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(123L))
                .andExpect(jsonPath("$.key").value("Resource-123-Key"))
                .andExpect(jsonPath("$.type").value("Resource-123-Type"))
                .andExpect(jsonPath("$.description").value("Resource 123 Description"));

        verify(resourceService).getResourceById(123L);
    }

    @Test
    void getResourceById_notFound() throws Exception {
        when(resourceService.getResourceById(987L)).thenThrow(new ClientResourceAccessApiRuntimeException("Resource 987 Not Found"));

        mockMvc.perform(get("/api/v1/resource/987"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void getResourceById_invalidId() throws Exception {
        mockMvc.perform(get("/api/v1/resource/A1B2C3"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void deleteResource_success() throws Exception {
        Mockito.doNothing().when(resourceService).deleteResourceById(135L);

        mockMvc.perform(delete("/api/v1/resource/10"))
                .andExpect(status().isNoContent());

        verify(resourceService).deleteResourceById(10L);
    }

    @Test
    void deleteResource_notFound() throws Exception {
        //when(resourceService.getResourceById(654L)).thenThrow(new ClientResourceAccessApiRuntimeException("Resource 654 Not Found"));
        doThrow(new ClientResourceAccessApiRuntimeException("Resource 654 Not Found"))
                .when(resourceService).deleteResourceById(654L);

        mockMvc.perform(delete("/api/v1/resource/654"))
                .andExpect(status().isNotFound());
    }


    @Test
    void getResourceKeyById_success() throws Exception {
        when(resourceService.getKeyById(579L)).thenReturn("This is the Resource 579 type");

        mockMvc.perform(get("/api/v1/resource/579/key"))
                .andExpect(status().isOk())
                .andExpect(content().string("This is the Resource 579 type"));
    }

    @Test
    void getResourceTypeById_success() throws Exception {
        when(resourceService.getDescriptionById(321L)).thenReturn("Linux Server #321 Description");

        mockMvc.perform(get("/api/v1/resource/321/description"))
                .andExpect(status().isOk())
                .andExpect(content().string("Linux Server #321 Description"));
    }

    @Test
    void getResourceDescriptionById_success() throws Exception {
        when(resourceService.getDescriptionById(456L)).thenReturn("#456 liner solution");

        mockMvc.perform(get("/api/v1/resource/456/description"))
                .andExpect(status().isOk())
                .andExpect(content().string("#456 liner solution"));
    }


    @Test
    void searchResources_noParameters() throws Exception {
        when(resourceService.searchResources(null,null,null))
                .thenReturn(List.of(
                        mockedResponse(111L,
                            "Resource-111-Key",
                            "Resource-111-Type",
                            "This is Resource 111 Description: need quick fix"),
                        mockedResponse(222L,
                                "Resource-222-Key",
                                "Resource-222-Type",
                                "This is Resource 222 Description: need quick fix")));

        mockMvc.perform(get("/api/v1/resource/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(111L))
                .andExpect(jsonPath("$[0].key").value("Resource-111-Key"))
                .andExpect(jsonPath("$[0].type").value("Resource-111-Type"))
                .andExpect(jsonPath("$[0].description").value("This is Resource 111 Description: need quick fix"))
                .andExpect(jsonPath("$[1].id").value(222L))
                .andExpect(jsonPath("$[1].key").value("Resource-222-Key"))
                .andExpect(jsonPath("$[1].type").value("Resource-222-Type"))
                .andExpect(jsonPath("$[1].description").value("This is Resource 222 Description: need quick fix"));
    }

    @Test
    void searchResources_byKey() throws Exception {
        when(resourceService.searchResources(eq("KEY-369"), isNull(), isNull()))
                .thenReturn(List.of(mockedResponse(369L,
                        "KEY-369",
                        "TYPE-369",
                        "Description-369: this is in the middle")));

        mockMvc.perform(get("/api/v1/resource/search")
                        .param("key","KEY-369"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(369L))
                .andExpect(jsonPath("$[0].key").value("KEY-369"))
                .andExpect(jsonPath("$[0].type").value("TYPE-369"))
                .andExpect(jsonPath("$[0].description").value("Description-369: this is in the middle"));
    }

    @Test
    void searchResources_byType() throws Exception {
        when(resourceService.searchResources(null,"123-Type",null))
                .thenReturn(List.of(mockedResponse(123L,
                        "123-Key",
                        "123-Type",
                        null)));

        mockMvc.perform(get("/api/v1/resource/search")
                        .param("type","123-Type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(123L))
                .andExpect(jsonPath("$[0].key").value("123-Key"))
                .andExpect(jsonPath("$[0].type").value("123-Type"))
                .andExpect(jsonPath("$[0].description").isEmpty());
    }

    @Test
    void searchResources_byDescription() throws Exception {
        when(resourceService.searchResources(null, null, "The description"))
                .thenReturn(List.of(mockedResponse(567L,
                        "Key",
                        null,
                        "The description")));

        mockMvc.perform(get("/api/v1/resource/search")
                        .param("description","The description"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(567L))
                .andExpect(jsonPath("$[0].key").value("Key"))
                .andExpect(jsonPath("$[0].type").isEmpty())
                .andExpect(jsonPath("$[0].description").value("The description"));
    }

    @Test
    void searchResources_allParameters() throws Exception {
        when(resourceService.searchResources("Key", "Type", "Description"))
                .thenReturn(List.of(mockedResponse(123L,
                        "Key-123",
                        "Type-123",
                        "The #123 Description")));

        mockMvc.perform(get("/api/v1/resource/search")
                        .param("key","Key")
                        .param("type","Type")
                        .param("description","Description"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(123L))
                .andExpect(jsonPath("$[0].key").value("Key-123"))
                .andExpect(jsonPath("$[0].type").value("Type-123"))
                .andExpect(jsonPath("$[0].description").value("The #123 Description"));
    }

    @Test
    void searchResources_emptyResult() throws Exception {
        when(resourceService.searchResources(any(),any(),any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/resource/search"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }



    @Test
    void postRequest_success() throws Exception {
        ResourcePostRequest request = ResourcePostRequest.builder()
                        .key("App-Key")
                        .type("App-Type")
                        .description("App description")
                        .build();

        when(resourceService.createResource(any())).thenReturn(mockedResponse(
                789L,
                request.key(),
                request.type(),
                request.description()));

        mockMvc.perform(post("/api/v1/resource")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(789L))
                .andExpect(jsonPath("$.key").value(request.key()))
                .andExpect(jsonPath("$.type").value(request.type()))
                .andExpect(jsonPath("$.description").value(request.description()));
    }

    @Test
    void postRequest_missingBody() throws Exception {
        mockMvc.perform(post("/api/v1/resource")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    @Test
    void postRequest_invalidJson() throws Exception {
        mockMvc.perform(post("/api/v1/resource")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postRequest_blankKey() throws Exception {
        ResourcePostRequest request = ResourcePostRequest.builder().key("").build();

        mockMvc.perform(post("/api/v1/resource")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void putRequest_success() throws Exception {
        ResourcePutRequest request = ResourcePutRequest.builder()
                        .key("New-Key")
                                .type("New-Type")
                                        .description("New Description")
                                                .build();

        when(resourceService.replaceResource(eq(100L), any()))
                .thenReturn(mockedResponse(99L,
                        "New-Key-99",
                        "New-Type-99", "#99 New Description"));

        mockMvc.perform(put("/api/v1/resource/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99L))
                .andExpect(jsonPath("$.key").value("New-Key-99"))
                .andExpect(jsonPath("$.type").value("New-Type-99"))
                .andExpect(jsonPath("$.description").value("#99 New Description"));
    }

    @Test
    void putRequest_validationFailure() throws Exception {
        ResourcePutRequest request = ResourcePutRequest.builder().key("").build();

        mockMvc.perform(put("/api/v1/resource/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchRequest_success() throws Exception {
        ResourcePatchRequest request = ResourcePatchRequest.builder().description("Updated description").build();

        when(resourceService.updateResource(eq(100L), any()))
                .thenReturn(mockedResponse(100L,
                        "Key-100",
                        null,
                        "Updated description"));

        mockMvc.perform(patch("/api/v1/resource/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.key").value("Key-100"))
                .andExpect(jsonPath("$.type").isEmpty())
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void patchRequest_validationFailure() throws Exception {
        ResourcePatchRequest request = ResourcePatchRequest.builder().key("").build();

        mockMvc.perform(patch("/api/v1/resource/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }


    /* PRIVATE */
    private ResourceResponse mockedResponse(Long resourceId,
                                            String resourceKey,
                                            String resourceType,
                                            String resourceDescription)
    {
        return ResourceResponse.builder()
                .id(resourceId)
                .key(resourceKey)
                .type(resourceType)
                .description(resourceDescription)
                .build();
    }

}