package com.dreamtech.clientresourceaccessapi.repository;

import com.dreamtech.clientresourceaccessapi.model.ClientResourceAccess;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles({"h2mem-test", "test"})
class ClientResourceAccessRepositoryTest {
    /*  Pure Unit Test.
        Unit test for ClientResourceAccessRepository using the H2 in-memory database.
        Note: More test cases may be added later.   */

    @Autowired
    private Environment environment;

    @Autowired
    private ClientResourceAccessRepository repository;

    @Autowired
    private TestEntityManager entityManager;


    @Test
    void checkActiveProfile() {
        String[] env = environment.getActiveProfiles();
        Assertions.assertThat(Arrays.toString(env)).isEqualTo("[h2mem-test, test]");
    }


    @Test
    void findByAccessCodeAndDescription() {
        entityManager.persist(entity(1001L,2001L,"1-ACCESS-CODE-1","1-DESCRIPTION-1"));
        entityManager.persist(entity(1002L,2002L,"2-ACCESS-CODE","2-Description"));
        entityManager.persist(entity(1003L,2003L,"Access-Code","DESCRIPTION"));

        entityManager.flush();

        List<ClientResourceAccess> list = repository.findByAccessCodeAndDescription(
                        "ACCESS-CODE",
                        "DESCRIPTION");

        assertThat(list).hasSize(1);

        assertThat(list.getFirst().getClientId()).isEqualTo(1001L);
        assertThat(list.getFirst().getResourceId()).isEqualTo(2001L);
        assertThat(list.getFirst().getAccessCode()).isEqualTo("1-ACCESS-CODE-1");
        assertThat(list.getFirst().getDescription()).isEqualTo("1-DESCRIPTION-1");
    }

    @Test
    void findByAccessCode() {
        entityManager.persist(entity(1004L,2004L,"1-ACCESS-CODE","DESCRIPTION"));
        entityManager.persist(entity(1005L,2005L,"ACCESS-CODE-2","A"));
        entityManager.persist(entity(1006L,3006L,"ACCESS CODE","B"));

        entityManager.flush();

        List<ClientResourceAccess> list = repository.findByAccessCode("ACCESS-CODE");

        assertThat(list).hasSize(2);

        assertThat(list.getFirst().getClientId()).isEqualTo(1004L);
        assertThat(list.getFirst().getResourceId()).isEqualTo(2004L);
        assertThat(list.getFirst().getAccessCode()).isEqualTo("1-ACCESS-CODE");
        assertThat(list.getFirst().getDescription()).isEqualTo("DESCRIPTION");

        assertThat(list.get(1).getClientId()).isEqualTo(1005L);
        assertThat(list.get(1).getResourceId()).isEqualTo(2005L);
        assertThat(list.get(1).getAccessCode()).isEqualTo("ACCESS-CODE-2");
        assertThat(list.get(1).getDescription()).isEqualTo("A");
    }


    @Test
    void findByDescription() {
        entityManager.persist(entity(1007L,2007L,"A","1-Description"));
        entityManager.persist(entity(1008L,2008L,"B","DESCRIPTION-2"));
        entityManager.persist(entity(1009L,2009L,"C","3-Description-3"));

        entityManager.flush();

        List<ClientResourceAccess> list = repository.findByDescription("Description");

        assertThat(list).hasSize(2);

        assertThat(list.getFirst().getClientId()).isEqualTo(1007L);
        assertThat(list.getFirst().getResourceId()).isEqualTo(2007L);
        assertThat(list.getFirst().getAccessCode()).isEqualTo("A");
        assertThat(list.getFirst().getDescription()).isEqualTo("1-Description");

        assertThat(list.get(1).getClientId()).isEqualTo(1009L);
        assertThat(list.get(1).getResourceId()).isEqualTo(2009L);
        assertThat(list.get(1).getAccessCode()).isEqualTo("C");
        assertThat(list.get(1).getDescription()).isEqualTo("3-Description-3");
    }

    @Test
    void findByIds_found() {
        // Given
        ClientResourceAccess entity = entity(1010L,2010L,"ACCESS-CODE","DESCRIPTION Only");

        entityManager.persistAndFlush(entity);

        // When
        Optional<ClientResourceAccess> result = repository.findByIds(1010L,2010L);

        // Then
        assertThat(result).isPresent();

        assertThat(result.get().getAccessCode()).isEqualTo("ACCESS-CODE");
        assertThat(result.get().getDescription()).isEqualTo("DESCRIPTION Only");
    }

    @Test
    void findByIds_notFound() {
        // When
        Optional<ClientResourceAccess> result = repository.findByIds(1011L,2011L);

        // Then
        assertThat(result).isEmpty();
    }


    @Test
    void findAccessCodeByIds_found() {
        entityManager.persistAndFlush(entity(1012L,2012L,"CODE","Description"));

        Optional<String> result = repository.findAccessCodeByIds(1012L,2012L);

        assertThat(result).contains("CODE");
    }


    @Test
    void findDescriptionByIds_found() {
        entityManager.persistAndFlush(entity(1013L,2013L,"ACCESS-CODE","Description Only"));

        Optional<String> result = repository.findDescriptionByIds(1013L,2013L);

        assertThat(result).contains("Description Only");
    }


    @Test
    void deleteByIds() {
        entityManager.persistAndFlush(entity(1014L,2014L,"ACCESS-CODE","Description"));

        repository.deleteByIds(1014L,2014L);

        entityManager.flush();

        Optional<ClientResourceAccess> result = repository.findByIds(1014L,2014L);

        assertThat(result).isEmpty();
    }


    /*   PRIVATE   */
    private ClientResourceAccess entity(
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