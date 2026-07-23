package com.dreamtech.clientresourceaccessapi.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import com.dreamtech.clientresourceaccessapi.dto.ClientResourceAccessPatchRequest;
import com.dreamtech.clientresourceaccessapi.dto.ClientResourceAccessPostRequest;
import com.dreamtech.clientresourceaccessapi.dto.ClientResourceAccessPutRequest;
import com.dreamtech.clientresourceaccessapi.dto.ClientResourceAccessResponse;
import com.dreamtech.clientresourceaccessapi.exception.ClientResourceAccessApiRuntimeException;
import com.dreamtech.clientresourceaccessapi.model.ClientResourceAccess;
import com.dreamtech.clientresourceaccessapi.repository.ClientResourceAccessRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test") // Activates application-test.yml
class ClientResourceAccessServiceImplTest {
    /*  Pure Unit Test.
        Unit Test for ClientResourceAccessServiceImpl using Mock & InjectMocks.   */

    @Mock
    private ClientResourceAccessRepository repository;

    @InjectMocks
    private ClientResourceAccessServiceImpl service;

    @Autowired
    private Environment environment;


    @Test
    void testActiveProfile() {
        String[] env = environment.getActiveProfiles();
        assertThat(Arrays.toString(env)).isEqualTo("[test]");
    }

    @Test
    void createClientResourceAccess_success() {
        // Given
        ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                .clientId(1001L)
                .resourceId(2001L)
                .accessCode("ACCESS-1001-2001")
                .description("Description-1001-2001")
                .build();

        ClientResourceAccess entity = mockedEntity(
                        1101L,
                        2201L,
                        "ACCESS-1101-2201",
                        "Description-1101-2201");

        given(repository.findByIds(1001L,2001L)).willReturn(Optional.empty());

        given(repository.save(any(ClientResourceAccess.class))).willReturn(entity);

        // When
        ClientResourceAccessResponse response = service.createClientResourceAccess(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.clientId()).isEqualTo(1101L);
        assertThat(response.resourceId()).isEqualTo(2201L);
        assertThat(response.accessCode()).isEqualTo("ACCESS-1101-2201");
        assertThat(response.description()).isEqualTo("Description-1101-2201");

        then(repository)
                .should()
                .save(any(ClientResourceAccess.class));
    }

    @Test
    void createClientResourceAccess_duplicate() {
        String  exceptionMsg = "ClientResourceAccess already exists with (clientId,resourceId): (1002,2002)";
        ClientResourceAccess existing =  mockedEntity(
                        1002L,
                        2002L,
                        "ACCESS-002-Existing",
                        "Description 002-Existing");

        given(repository.findByIds(1002L,2002L))
                .willReturn(Optional.of(existing));

        ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                        .clientId(1002L)
                        .resourceId(2002L)
                        .accessCode("ACCESS-002-New")
                        .description("Description 002 New")
                        .build();

        assertThatThrownBy(() -> service.createClientResourceAccess(request))
                .isInstanceOf(ClientResourceAccessApiRuntimeException.class)
                .hasMessageContaining(exceptionMsg);

        then(repository)
                .should(never())
                .save(any());
    }


    @Test
    void searchClientResourceAccesses_accessCode() {
        given(repository.findByAccessCode("ACCESS-CODE"))
                .willReturn(List.of(
                        mockedEntity(
                                1003L,
                                2003L,
                                "ACCESS-CODE",
                                "DESCRIPTION-1003-2003")));

        List<ClientResourceAccessResponse> result = service.searchClientResourceAccesses("ACCESS-CODE", null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().clientId()).isEqualTo(1003L);
        assertThat(result.getFirst().resourceId()).isEqualTo(2003L);
        assertThat(result.getFirst().accessCode()).isEqualTo("ACCESS-CODE");
        assertThat(result.getFirst().description()).isEqualTo("DESCRIPTION-1003-2003");

        then(repository)
                .should()
                .findByAccessCode("ACCESS-CODE");
    }

    @Test
    void searchClientResourceAccesses_description() {
        given(repository.findByDescription("DESCRIPTION"))
                .willReturn(List.of(
                        mockedEntity(
                                1004L,
                                2004L,
                                "ACCESS-CODE",
                                "DESCRIPTION")));

        List<ClientResourceAccessResponse> result = service.searchClientResourceAccesses(null, "DESCRIPTION");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().clientId()).isEqualTo(1004L);
        assertThat(result.getFirst().resourceId()).isEqualTo(2004L);
        assertThat(result.getFirst().accessCode()).isEqualTo("ACCESS-CODE");
        assertThat(result.getFirst().description()).isEqualTo("DESCRIPTION");

        then(repository)
                .should()
                .findByDescription("DESCRIPTION");
    }

    @Test
    void searchClientResourceAccesses_accessCode_description() {
        given(repository.findByAccessCodeAndDescription("ACCESS-CODE", "DESCRIPTION"))
                .willReturn(List.of(
                        mockedEntity(
                                1105L,
                                2105L,
                                "ACCESS-CODE-105",
                                "DESCRIPTION-105"),
                        mockedEntity(
                                1205L,
                                2205L,
                                "ACCESS-CODE-205",
                                "DESCRIPTION-205"))

                        );

        List<ClientResourceAccessResponse> result = service.searchClientResourceAccesses("ACCESS-CODE", "DESCRIPTION");

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().clientId()).isEqualTo(1105L);
        assertThat(result.getFirst().resourceId()).isEqualTo(2105L);
        assertThat(result.getFirst().accessCode()).isEqualTo("ACCESS-CODE-105");
        assertThat(result.getFirst().description()).isEqualTo("DESCRIPTION-105");

        assertThat(result.get(1).clientId()).isEqualTo(1205L);
        assertThat(result.get(1).resourceId()).isEqualTo(2205L);
        assertThat(result.get(1).accessCode()).isEqualTo("ACCESS-CODE-205");
        assertThat(result.get(1).description()).isEqualTo("DESCRIPTION-205");

        then(repository)
                .should()
                .findByAccessCodeAndDescription("ACCESS-CODE", "DESCRIPTION");
    }



    @Test
    void replaceClientResourceAccess_existing() {
        ClientResourceAccess existing = mockedEntity(
                        1006L,
                        2006L,
                        "ACCESS-OLD",
                        "DESCRIPTIONOld");

        given(repository.findByIds(1006L,2006L)).willReturn(Optional.of(existing));

        given(repository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        ClientResourceAccessPutRequest request = ClientResourceAccessPutRequest.builder()
                .accessCode("NEW")
                .description("Updated")
                .build();

        ClientResourceAccessResponse response = service.replaceClientResourceAccess(
                        1006L,
                        2006L,
                        request);

        assertThat(response).isNotNull();
        assertThat(response.clientId()).isEqualTo(1006L);
        assertThat(response.resourceId()).isEqualTo(2006L);
        assertThat(response.accessCode()).isEqualTo("NEW");
        assertThat(response.description()).isEqualTo("Updated");

        then(repository)
                .should()
                .save(any());
    }

    @Test
    void replaceClientResourceAccess_newEntity() {
        given(repository.findByIds(1007L,2007L)).willReturn(Optional.empty());

        given(repository.save(any())).willAnswer(i -> i.getArgument(0));

        ClientResourceAccessPutRequest request = ClientResourceAccessPutRequest.builder()
                .accessCode("NEW")
                .description("Created")
                .build();

        ClientResourceAccessResponse response = service.replaceClientResourceAccess(1007L, 2007L, request);

        assertThat(response).isNotNull();
        assertThat(response.clientId()).isEqualTo(1007L);
        assertThat(response.resourceId()).isEqualTo(2007L);
        assertThat(response.accessCode()).isEqualTo("NEW");
        assertThat(response.description()).isEqualTo("Created");

        then(repository)
                .should()
                .save(any());
    }


    @Test
    void updateClientResourceAccess_accessCodeOnly() {
        ClientResourceAccess entity = mockedEntity(
                        null,
                        null,
                        "OLD",
                        null);

        given(repository.findByIds(1008L,2008L)).willReturn(Optional.of(entity));

        given(repository.save(any()))
                .willAnswer(i -> i.getArgument(0));

        ClientResourceAccessPatchRequest request = ClientResourceAccessPatchRequest.builder()
                .accessCode("NEW").description(null).build();

        ClientResourceAccessResponse response = service.updateClientResourceAccess(
                        1008L,
                        2008L,
                        request);

        assertThat(response.accessCode()).isEqualTo("NEW");
    }

    @Test
    void updateClientResourceAccess_notFound() {
        given(repository.findByIds(1009L,2009L)).willReturn(Optional.empty());

        ClientResourceAccessPatchRequest request = ClientResourceAccessPatchRequest.builder()
                .accessCode("NEW").description("Desc").build();

        assertThatThrownBy(() -> service.updateClientResourceAccess(
                        1009L,
                        2009L,
                        request))
                .isInstanceOf(ClientResourceAccessApiRuntimeException.class);
    }


    @Test
    void deleteByIds_success() {
        ClientResourceAccess entity = mockedEntity(
                        1010L,
                        2010L,
                        "AAA",
                        "BBB");

        given(repository.findByIds(1010L,2010L))
                .willReturn(Optional.of(entity));

        willDoNothing()
                .given(repository)
                .deleteByIds(1010L,2010L);

        service.deleteByIds(1010L,2010L);

        then(repository)
                .should()
                .deleteByIds(1010L,2010L);
    }

    @Test
    void deleteByIds_notFound() {
        given(repository.findByIds(1011L,2011L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.deleteByIds(1011L,2011L))
                .isInstanceOf(ClientResourceAccessApiRuntimeException.class);

        then(repository)
                .should(never())
                .deleteByIds(anyLong(), anyLong());
    }


    @Test
    void getByIds_success() {
        ClientResourceAccess entity = mockedEntity(
                        1012L,
                        2012L,
                        "ACCESS",
                        "DESCRIPTION");

        given(repository.findByIds(1012L,2012L))
                .willReturn(Optional.of(entity));

        ClientResourceAccessResponse response = service.getByIds(1012L,2012L);

        assertThat(response).isNotNull();
        assertThat(response.clientId()).isEqualTo(1012L);
        assertThat(response.resourceId()).isEqualTo(2012L);
        assertThat(response.accessCode()).isEqualTo("ACCESS");
        assertThat(response.description()).isEqualTo("DESCRIPTION");

        then(repository)
                .should()
                .findByIds(1012L,2012L);
    }

    @Test
    void getByIds_notFound() {
        given(repository.findByIds(1013L,2013L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByIds(1013L,2013L))
                .isInstanceOf(ClientResourceAccessApiRuntimeException.class);

        then(repository)
                .should()
                .findByIds(1013L,2013L);
    }


    @Test
    void getAccessCodeByIds_success() {
        given(repository.findAccessCodeByIds(1014L,2014L))
                .willReturn(Optional.of("AccessCode"));

        String code = service.getAccessCodeByIds(1014L,2014L);

        assertThat(code).isEqualTo("AccessCode");
    }

    @Test
    void getAccessCodeByIds_notFound() {
        given(repository.findAccessCodeByIds(1015L,2015L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAccessCodeByIds(1015L,2015L))
                .isInstanceOf(ClientResourceAccessApiRuntimeException.class);
    }


    @Test
    void getDescriptionByIds_success() {
        given(repository.findDescriptionByIds(1016L,2016L))
                .willReturn(Optional.of("DESCRIPTION-016"));

        String code = service.getDescriptionByIds(1016L,2016L);

        assertThat(code).isEqualTo("DESCRIPTION-016");
    }

    @Test
    void getDescriptionByIds_notFound() {
        given(repository.findDescriptionByIds(1017L,2017L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDescriptionByIds(1017L,2017L))
                .isInstanceOf(ClientResourceAccessApiRuntimeException.class);
    }



    /*   PRIVATE   */
    private ClientResourceAccess mockedEntity(
            Long clientId,
            Long resourceId,
            String accessCode,
            String description) {

        return ClientResourceAccess.builder()
                .clientId(clientId)
                .resourceId(resourceId)
                .accessCode(accessCode)
                .description(description)
                .build();
    }

}