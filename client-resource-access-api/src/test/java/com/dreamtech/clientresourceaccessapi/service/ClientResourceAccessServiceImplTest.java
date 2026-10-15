package com.dreamtech.clientresourceaccessapi.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

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
@ActiveProfiles("h2mem-test") // Activates application-h2mem-test.yml
class ClientResourceAccessServiceImplTest {
    /*  Pure Unit Test.
        Unit Test for ClientResourceAccessServiceImpl using Mock & InjectMocks.   */


    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ResourceRepository resourceRepository;
    @Mock
    private ClientResourceAccessRepository repository;

    @InjectMocks
    private ClientResourceAccessServiceImpl service;

    @Autowired
    private Environment environment;


    @Test
    void testActiveProfile() {
        String[] env = environment.getActiveProfiles();
        assertThat(Arrays.toString(env)).isEqualTo("[h2mem-test]");
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
        given(clientRepository.findById(1001L)).willReturn(Optional.of(new Client()));
        given(resourceRepository.findById(2001L)).willReturn(Optional.of(new Resource()));

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
    void createClientResourceAccess_exception_duplicate() {
        String  msg409 = "ClientResourceAccess already exists with (clientId,resourceId): (1002,2002)";

        given(clientRepository.findById(1002L)).willReturn(Optional.of(new Client()));
        given(resourceRepository.findById(2002L)).willReturn(Optional.of(new Resource()));
        given(repository.findByIds(1002L,2002L)).willReturn(Optional.of(new ClientResourceAccess()));

        ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                        .clientId(1002L)
                        .resourceId(2002L)
                        .accessCode("ACCESS-002-New")
                        .description("Description 002 New")
                        .build();

        assertThatThrownBy(() -> service.createClientResourceAccess(request))
                .isInstanceOf(HttpStatus409AlreadyExistsException.class)
                .hasMessageContaining(msg409);

        then(repository)
                .should(never())
                .save(any());
    }

    @Test
    void createClientResourceAccess_exception_invalid_client() {
        String  msg404 = "Client not found with id: 1102";

        given(clientRepository.findById(1102L)).willReturn(Optional.empty());

        ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                .clientId(1102L)
                .resourceId(2102L)
                .accessCode("ACCESS-102")
                .description("Description 102")
                .build();

        assertThatThrownBy(() -> service.createClientResourceAccess(request))
                .isInstanceOf(HttpStatus404NotFoundException.class)
                .hasMessageContaining(msg404);

        then(repository)
                .should(never())
                .save(any());
    }

    @Test
    void createClientResourceAccess_exception_invalid_resource() {
        String  msg404 = "Resource not found with id: 2102";

        given(clientRepository.findById(1102L)).willReturn(Optional.of(new Client()));
        given(resourceRepository.findById(2102L)).willReturn(Optional.empty());

        ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                .clientId(1102L)
                .resourceId(2102L)
                .accessCode("ACCESS-102")
                .description("Description 102")
                .build();

        assertThatThrownBy(() -> service.createClientResourceAccess(request))
                .isInstanceOf(HttpStatus404NotFoundException.class)
                .hasMessageContaining(msg404);

        then(repository)
                .should(never())
                .save(any());
    }

    @Test
    void createClientResourceAccess_non_existing_client() {
        String  msg404 = "Client not found with id: 1101";
        given(clientRepository.findById(1101L)).willReturn(Optional.empty());

        ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                .clientId(1101L)
                .resourceId(2201L)
                .accessCode("ACCESS-201-New")
                .description("Description 201 New")
                .build();

        assertThatThrownBy(() -> service.createClientResourceAccess(request))
                .isInstanceOf(HttpStatus404NotFoundException.class)
                .hasMessageContaining(msg404);

        then(repository)
                .should(never())
                .save(any());
    }


    @Test
    void createClientResourceAccess_non_existing_resource() {
        String  msg404 = "Resource not found with id: 2202";
        given(clientRepository.findById(1102L)).willReturn(Optional.of(new Client()));
        given(resourceRepository.findById(2202L)).willReturn(Optional.empty());

        ClientResourceAccessPostRequest request = ClientResourceAccessPostRequest.builder()
                .clientId(1102L)
                .resourceId(2202L)
                .accessCode("ACCESS-202-New")
                .description("Description 202 New")
                .build();

        assertThatThrownBy(() -> service.createClientResourceAccess(request))
                .isInstanceOf(HttpStatus404NotFoundException.class)
                .hasMessageContaining(msg404);

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

        given(clientRepository.findById(1006L)).willReturn(Optional.of(new Client()));
        given(resourceRepository.findById(2006L)).willReturn(Optional.of(new Resource()));
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
        given(clientRepository.findById(1007L)).willReturn(Optional.of(new Client()));
        given(resourceRepository.findById(2007L)).willReturn(Optional.of(new Resource()));
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
    void replaceClientResourceAccess_invalid_client() {
        long client_id = 1007L;
        long resource_id = 2007L;
        String  msg404 = "Client not found with id: 1007";

        given(clientRepository.findById(1007L)).willReturn(Optional.empty());

        ClientResourceAccessPutRequest request = ClientResourceAccessPutRequest.builder()
                .accessCode("NEW")
                .description("Created")
                .build();

        assertThatThrownBy(() ->service.replaceClientResourceAccess(client_id, resource_id, request))
                .isInstanceOf(HttpStatus404NotFoundException.class)
                .hasMessageContaining(msg404);
    }

    @Test
    void replaceClientResourceAccess_invalid_resource() {
        long client_id = 1007L;
        long resource_id = 2007L;
        String  msg404 = "Resource not found with id: 2007";

        given(clientRepository.findById(1007L)).willReturn(Optional.of(new Client()));
        given(resourceRepository.findById(2007L)).willReturn(Optional.empty());

        ClientResourceAccessPutRequest request = ClientResourceAccessPutRequest.builder()
                .accessCode("NEW")
                .description("Created")
                .build();

        assertThatThrownBy(() ->service.replaceClientResourceAccess(client_id, resource_id, request))
                .isInstanceOf(HttpStatus404NotFoundException.class)
                .hasMessageContaining(msg404);
    }


    @Test
    void updateClientResourceAccess_accessCodeOnly() {
        ClientResourceAccess entity = mockedEntity(
                        null,
                        null,
                        "OLD",
                        null);

        given(clientRepository.findById(1008L)).willReturn(Optional.of(new Client()));
        given(resourceRepository.findById(2008L)).willReturn(Optional.of(new Resource()));
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
        String msg404 = "ClientResourceAccess not found with (clientId,resourceId): (1009,2009)";

        given(repository.findByIds(1009L,2009L)).willReturn(Optional.empty());

        ClientResourceAccessPatchRequest request = ClientResourceAccessPatchRequest.builder()
                .accessCode("NEW").description("Desc").build();

        assertThatThrownBy(() -> service.updateClientResourceAccess(
                        1009L,
                        2009L,
                        request))
                .isInstanceOf(HttpStatus404NotFoundException.class)
                .hasMessageContaining(msg404);
    }

    @Test
    void updateClientResourceAccess_client_notFound() {
        String  msg404 = "Client not found with id: 1109";

        given(repository.findByIds(1109L,2209L)).willReturn(Optional.of(new ClientResourceAccess()));
        given(clientRepository.findById(1109L)).willReturn(Optional.empty());

        ClientResourceAccessPatchRequest request = ClientResourceAccessPatchRequest.builder()
                .accessCode("NEW").description("Desc").build();

        assertThatThrownBy(() -> service.updateClientResourceAccess(
                1109L,
                2209L,
                request))
                .isInstanceOf(HttpStatus404NotFoundException.class)
                .hasMessageContaining(msg404);
    }


    @Test
    void updateClientResourceAccess_resource_notFound() {
        String  msg404 = "Resource not found with id: 2222";

        given(repository.findByIds(1111L, 2222L)).willReturn(Optional.of(new ClientResourceAccess()));
        given(clientRepository.findById(1111L)).willReturn(Optional.of(new Client()));
        given(resourceRepository.findById(2222L)).willReturn(Optional.empty());

        ClientResourceAccessPatchRequest request = ClientResourceAccessPatchRequest.builder()
                .accessCode("NEW").description("Desc").build();

        assertThatThrownBy(() -> service.updateClientResourceAccess(
                1111L,
                2222L,
                request))
                .isInstanceOf(HttpStatus404NotFoundException.class)
                .hasMessageContaining(msg404);
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
                .isInstanceOf(HttpStatus404NotFoundException.class);

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
                .isInstanceOf(HttpStatus404NotFoundException.class);

        then(repository)
                .should()
                .findByIds(1013L,2013L);
    }


    @Test
    void getAccessCodeByIds_success() {
        long client_id = 1014L;
        long resource_id = 2014L;
        String mockedAccessCode = "AccessCode-014";
        ClientResourceAccess cra = new ClientResourceAccess();
        cra.setClientId(client_id);
        cra.setResourceId(resource_id);
        cra.setAccessCode(mockedAccessCode);

        //given(repository.findAccessCodeByIds(client_id, resource_id)).willReturn(Optional.of("AccessCode"));
        given(repository.findByIds(client_id, resource_id)).willReturn(Optional.of(cra));

        String code = service.getAccessCodeByIds(client_id, resource_id);

        assertThat(code).isEqualTo(mockedAccessCode);
    }

    @Test
    void getAccessCodeByIds_notFound() {
        long client_id = 1015L;
        long resource_id = 2015L;

        //given(repository.findAccessCodeByIds(client_id, resource_id)).willReturn(Optional.empty());
        given(repository.findByIds(client_id, resource_id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAccessCodeByIds(client_id, resource_id))
                .isInstanceOf(HttpStatus404NotFoundException.class);
    }


    @Test
    void getDescriptionByIds_success() {
        long client_id = 1016L;
        long resource_id = 2016L;
        String mockedDescription = "DESCRIPTION-016";
        ClientResourceAccess cra = new ClientResourceAccess();
        cra.setClientId(client_id);
        cra.setResourceId(resource_id);
        cra.setDescription(mockedDescription);

        //given(repository.findDescriptionByIds(client_id, resource_id)).willReturn(Optional.of(mockedDescription));
        given(repository.findByIds(client_id, resource_id)).willReturn(Optional.of(cra));

        String description = service.getDescriptionByIds(client_id, resource_id);

        assertThat(description).isEqualTo(mockedDescription);
    }

    @Test
    void getDescriptionByIds_success_null_description() {
        long client_id = 1016L;
        long resource_id = 2016L;
        ClientResourceAccess cra = new ClientResourceAccess();
        cra.setClientId(client_id);
        cra.setResourceId(resource_id);

        //given(repository.findDescriptionByIds(client_id, resource_id)).willReturn(Optional.of(mockedDescription));
        given(repository.findByIds(client_id, resource_id)).willReturn(Optional.of(cra));

        String description = service.getDescriptionByIds(client_id, resource_id);

        assertThat(description).isNull();
    }


    @Test
    void getDescriptionByIds_notFound() {
        long client_id = 1017L;
        long resource_id = 2017L;

        //given(repository.findDescriptionByIds(client_id, resource_id)).willReturn(Optional.empty());
        given(repository.findByIds(client_id, resource_id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDescriptionByIds(client_id, resource_id))
                .isInstanceOf(HttpStatus404NotFoundException.class);
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