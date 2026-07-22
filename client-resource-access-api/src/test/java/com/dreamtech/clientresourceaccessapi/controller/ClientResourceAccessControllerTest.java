package com.dreamtech.clientresourceaccessapi.controller;

import com.dreamtech.clientresourceaccessapi.dto.ClientResourceAccessPatchRequest;
import com.dreamtech.clientresourceaccessapi.dto.ClientResourceAccessPostRequest;
import com.dreamtech.clientresourceaccessapi.dto.ClientResourceAccessPutRequest;
import com.dreamtech.clientresourceaccessapi.dto.ClientResourceAccessResponse;
import com.dreamtech.clientresourceaccessapi.exception.ClientResourceAccessApiRuntimeException;
import com.dreamtech.clientresourceaccessapi.service.ClientResourceAccessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@WebMvcTest(ClientResourceAccessController.class)
@ActiveProfiles("test") // Activates application-test.yml
class ClientResourceAccessControllerTest {
    /*  Pure Unit Test for ClientResourceAccessController.
        This is Pure Unit Test for ClientResourceAccessController using Autowired and MockitoBean.
        Note: More test cases may be added later.   */

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClientResourceAccessService service;

    @Autowired
    private Environment environment;


    @Test
    void testActiveProfile() {
        String[] env = environment.getActiveProfiles();
        assertThat(Arrays.toString(env)).isEqualTo("[test]");
    }

    @Test
    void getByIds_success() throws Exception {
        // Given
        ClientResourceAccessResponse response = mockedResponse(
                1001L,2001L, "Access-Code-1001-2001", "The Description");

        given(service.getByIds(1001L,2001L)).willReturn(response);

        // When / Then
        mockMvc.perform(get("/api/v1/client-resource-access/client/1001/resource/2001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$.clientId").value(1001))
                .andExpect(jsonPath("$.resourceId").value(2001))
                .andExpect(jsonPath("$.accessCode").value("Access-Code-1001-2001"))
                .andExpect(jsonPath("$.description").value("The Description"));

        then(service)
                .should()
                .getByIds(1001L,2001L);
    }

    @Test
    void getByIds_notFound() throws Exception {
        // Given
        given(service.getByIds(1002L, 2002L))
                .willThrow(new ClientResourceAccessApiRuntimeException("1002 2002 Not Found"));

        // When / Then
        mockMvc.perform(get("/api/v1/client-resource-access/client/1002/resource/2002"))
                .andExpect(status().isNotFound());

        then(service)
                .should()
                .getByIds(1002L,2002L);
    }



    @Test
    void deleteByIds_success() throws Exception {
        willDoNothing()
                .given(service)
                .deleteByIds(1003L,2003L);

        mockMvc.perform(delete("/api/v1/client-resource-access/client/1003/resource/2003"))
                .andExpect(status().isNoContent());

        then(service)
                .should()
                .deleteByIds(1003L,2003L);
    }

    @Test
    void deleteByIds_notFound() throws Exception {
        willThrow(new ClientResourceAccessApiRuntimeException("1004 2004 Not Found"))
                .given(service)
                .deleteByIds(1004L, 2004L);

        mockMvc.perform(delete("/api/v1/client-resource-access/client/1004/resource/2004"))
                .andExpect(status().isNotFound());

        then(service)
                .should()
                .deleteByIds(1004L,2004L);
    }


    @Test
    void getAccessCodeByIds_success() throws Exception {
        given(service.getAccessCodeByIds(1005L, 2005L))
                .willReturn("AccessCode-1005-2005");

        mockMvc.perform(get("/api/v1/client-resource-access/client/1005/resource/2005/access-code"))
                .andExpect(status().isOk())
                .andExpect(content().string("AccessCode-1005-2005"));

        then(service)
                .should()
                .getAccessCodeByIds(1005L,2005L);
    }

    @Test
    void getAccessCodeByIds_notFound() throws Exception {
        // Given
        given(service.getAccessCodeByIds(1006L, 2006L))
                .willThrow(new ClientResourceAccessApiRuntimeException("1006 2006 Not Found"));

        // When / Then
        mockMvc.perform(get("/api/v1/client-resource-access/client/1006/resource/2006/access-code"))
                .andExpect(status().isNotFound());

        then(service)
                .should()
                .getAccessCodeByIds(1006L,2006L);
    }


    @Test
    void getDescriptionByIds_success() throws Exception {
        given(service.getDescriptionByIds(1007L, 2007L))
                .willReturn("Description-1007-2007");

        mockMvc.perform(get("/api/v1/client-resource-access/client/1007/resource/2007/description"))
                .andExpect(status().isOk())
                .andExpect(content().string("Description-1007-2007"));

        then(service)
                .should()
                .getDescriptionByIds(1007L,2007L);
    }

    @Test
    void getDescriptionByIds_notFound() throws Exception {
        // Given
        given(service.getDescriptionByIds(1008L, 2008L))
                .willThrow(new ClientResourceAccessApiRuntimeException("1008 2008 Not Found"));

        // When / Then
        mockMvc.perform(get("/api/v1/client-resource-access/client/1008/resource/2008/description"))
                .andExpect(status().isNotFound());

        then(service)
                .should()
                .getDescriptionByIds(1008L,2008L);
    }


    @Test
    void searchClientResourceAccesses_byAccessCode_success() throws Exception {
        given(service.searchClientResourceAccesses(
                "AccessCode-1010-2010",
                null))
                .willReturn(List.of(mockedResponse(
                        1010L,
                        2010L,
                        "AccessCode-1010-2010",
                        "Description-1010-2010")));

        mockMvc.perform(get("/api/v1/client-resource-access/search")
                                .param("access-code","AccessCode-1010-2010"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].clientId").value(1010))
                .andExpect(jsonPath("$[0].resourceId").value(2010))
                .andExpect(jsonPath("$[0].accessCode").value("AccessCode-1010-2010"))
                .andExpect(jsonPath("$[0].description").value("Description-1010-2010"));

        then(service)
                .should()
                .searchClientResourceAccesses(
                        "AccessCode-1010-2010",
                        null);
    }

    @Test
    void searchClientResourceAccesses_byAccessCode_notFound() throws Exception {
        given(service.searchClientResourceAccesses(
                "AccessCode-1011-2011",
                null))
                .willReturn(List.of());

        mockMvc.perform(get("/api/v1/client-resource-access/search")
                        .param("access-code","AccessCode-1011-2011"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        then(service)
                .should()
                .searchClientResourceAccesses(
                        "AccessCode-1011-2011",
                        null);
    }

    @Test
    void searchClientResourceAccesses_byDescription_success() throws Exception {
        given(service.searchClientResourceAccesses(
                null,
                "Description-1012-2012"))
                .willReturn(List.of(mockedResponse(
                        1012L,
                        2012L,
                        "AccessCode-1012-2012",
                        "Description-1012-2012: Excellent !")));

        mockMvc.perform(get("/api/v1/client-resource-access/search")
                        .param("description","Description-1012-2012"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].clientId").value(1012))
                .andExpect(jsonPath("$[0].resourceId").value(2012))
                .andExpect(jsonPath("$[0].accessCode").value("AccessCode-1012-2012"))
                .andExpect(jsonPath("$[0].description").value("Description-1012-2012: Excellent !"));

        then(service)
                .should()
                .searchClientResourceAccesses(
                        null,
                        "Description-1012-2012");
    }



    @Test
    void postRequest_success() throws Exception {
        ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                .clientId(1013L)
                .resourceId(2013L)
                .accessCode("ACCESS-1013-2013")
                .description("1013-2013: Read Only")
                .build();

        given(service.createClientResourceAccess(any())).willReturn(mockedResponse(
                1113L,
                2213L,
                "AccessCode-112233",
                "Description-112233"));

        mockMvc.perform(post("/api/v1/client-resource-access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value(1113L))
                .andExpect(jsonPath("$.resourceId").value(2213L))
                .andExpect(jsonPath("$.accessCode").value("AccessCode-112233"))
                .andExpect(jsonPath("$.description").value("Description-112233"));

        then(service)
                .should()
                .createClientResourceAccess(any());
    }


    @Test
    void putClient_success() throws Exception {
        ClientResourceAccessPutRequest request = ClientResourceAccessPutRequest.builder()
                .accessCode("ACCESS-1014-2014")
                .description("Description-1014-2014-Updated")
                .build();

        given(service.replaceClientResourceAccess(
                eq(1014L),
                eq(2014L),
                any()))
                .willReturn(mockedResponse(1111L, 2222L,
                        "AccessCode-1111-2222", "Description-1111-2222-Update"));

        mockMvc.perform(put("/api/v1/client-resource-access/client/1014/resource/2014")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(1111L))
                .andExpect(jsonPath("$.resourceId").value(2222L))
                .andExpect(jsonPath("$.accessCode").value("AccessCode-1111-2222"))
                .andExpect(jsonPath("$.description").value("Description-1111-2222-Update"));

        then(service)
                .should()
                .replaceClientResourceAccess(
                        eq(1014L),
                        eq(2014L),
                        any());
    }


    @Test
    void patchClient_success() throws Exception {
        ClientResourceAccessPatchRequest request = ClientResourceAccessPatchRequest.builder()
                .accessCode(null)
                .description("Updated")
                .build();

        given(service.updateClientResourceAccess(
                eq(1015L),
                eq(2015L),
                any()))
                .willReturn(mockedResponse(10015L,
                        20015L,
                        "AccessCode-Updated",
                        "Description-Updated"));

        mockMvc.perform(patch("/api/v1/client-resource-access/client/1015/resource/2015")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(10015L))
                .andExpect(jsonPath("$.resourceId").value(20015L))
                .andExpect(jsonPath("$.accessCode").value("AccessCode-Updated"))
                .andExpect(jsonPath("$.description").value("Description-Updated"));

        then(service)
                .should()
                .updateClientResourceAccess(
                        eq(1015L),
                        eq(2015L),
                        any());
    }


    /*   PRIVATE   */
    private ClientResourceAccessResponse mockedResponse(
            Long clientId,
            Long resourceId,
            String accessCode,
            String description
    ) {
        return ClientResourceAccessResponse.builder()
                .clientId(clientId)
                .resourceId(resourceId)
                .accessCode(accessCode)
                .description(description)
                .build();
    }
}