package com.dreamtech.clientresourceaccessapi.repository;

import com.dreamtech.clientresourceaccessapi.model.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // Boots up ONLY the JPA components using the H2 in-memory database specified by the test YAML file
@ActiveProfiles("h2mem-test") // Loads application-h2mem-test.yml
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Forces Spring to use your YAML parameters
class ClientRepositoryTest {
    /*  Pure Unit Test.
        Unit Test for ClientRepository using the H2 in-memory database.
        Note: More test cases may be added later.   */

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private Environment environment;

    @Autowired
    private jakarta.persistence.EntityManager entityManager; // Inject EntityManager

    private final Long missingClientId = 987654321L;
    private final String missingClientKey = "MISSING-CLIENT-KEY";
    private final String missingClientDescription = "MISSING CLIENT DESCRIPTION";

    private final String existingClientKeyPattern = "Existing-Client-Key";
    private final String existingClientDescriptionPattern = "Existing Client Description";

    private Long existingClientId_1;
    private final String existingClientKey_1 = existingClientKeyPattern + "-First";
    private final String existingClientDescription_1 = existingClientDescriptionPattern + " First";

    private Long existingClientId_2;
    private final String existingClientKey_2 = "Second-" + existingClientKeyPattern;
    private final String existingClientDescription_2 = "Second " + existingClientDescriptionPattern;

    //private List<Client> listClient;

    @BeforeEach
    void setUp() {
        // The real H2 in-memory database is used here.
        clientRepository.deleteAll();

        // Reset the H2 auto-increment counter for your table
        entityManager.createNativeQuery("ALTER TABLE client ALTER COLUMN id RESTART WITH 1").executeUpdate();

        Client client_1 = new Client();
        client_1.setKey(existingClientKey_1);
        client_1.setDescription(existingClientDescription_1);

        client_1 = clientRepository.saveAndFlush(client_1);
        existingClientId_1 = client_1.getId();

        Client client_2 = new Client();
        client_2.setKey(existingClientKey_2);
        client_2.setDescription(existingClientDescription_2);

        client_2 = clientRepository.saveAndFlush(client_2);
        existingClientId_2 = client_2.getId();

        // Save to H2 and preserve the object (with its auto-generated ID) for verification
        //this.listClient = new ArrayList<>(clientRepository.findAll());
        //this.listClient.sort(Comparator.comparing(Client::getId));
    }


    @Test
    void checkActiveProfile() {
        // This will print the active profiles (e.g., [test]) helping you match the YAML suffix
        //System.out.println("Active profile: " + Arrays.toString(environment.getActiveProfiles()));
        String[] env = environment.getActiveProfiles();
        assertThat(Arrays.toString(env)).isEqualTo("[h2mem-test]");
    }


    @Test
    void findByKeyAndDescription() {
        // Arrange by setUp
        // Act: Try to fetch the user created during the setUp() phase
        List<Client> clientList = clientRepository.findByKeyAndDescription(existingClientKeyPattern, existingClientDescriptionPattern);

        // Assert

        assertThat(clientList).isNotNull();

        assertThat(clientList.size()).isEqualTo(2);
        assertThat(clientList.getFirst().getId()).isEqualTo(existingClientId_1);
        assertThat(clientList.getFirst().getKey()).isEqualTo(existingClientKey_1);
        assertThat(clientList.getFirst().getDescription()).isEqualTo(existingClientDescription_1);

        assertThat(clientList.get(1).getId()).isEqualTo(existingClientId_2);
        assertThat(clientList.get(1).getKey()).isEqualTo(existingClientKey_2);
        assertThat(clientList.get(1).getDescription()).isEqualTo(existingClientDescription_2);
    }


    @Test
    void findByKey_exists() {
        // Arrange by setUp
        // Act: Try to fetch the user created during the setUp() phase
        List<Client> clientList = clientRepository.findByKey(existingClientKeyPattern);

        // Assert
        assertThat(clientList).isNotNull();

        assertThat(clientList.size()).isEqualTo(2);
        assertThat(clientList.getFirst().getId()).isEqualTo(existingClientId_1);
        assertThat(clientList.getFirst().getKey()).isEqualTo(existingClientKey_1);
        assertThat(clientList.getFirst().getDescription()).isEqualTo(existingClientDescription_1);

        assertThat(clientList.get(1).getId()).isEqualTo(existingClientId_2);
        assertThat(clientList.get(1).getKey()).isEqualTo(existingClientKey_2);
        assertThat(clientList.get(1).getDescription()).isEqualTo(existingClientDescription_2);
    }

    @Test
    void findByKey_non_exist() {
        // Arrange by setUp
        // Act: Try to fetch the user created during the setUp() phase
        List<Client> clientList = clientRepository.findByKey(missingClientKey);
        // Assert
        assertThat(clientList).isNotNull();
        assertThat(clientList).isEmpty();
    }

    @Test
    void findByDescription_exist() {
        // Arrange by setUp
        // Act: Try to fetch the user created during the setUp() phase
        List<Client> clientList = clientRepository.findByDescription(existingClientDescriptionPattern);

        // Assert
        assertThat(clientList).isNotNull();

        assertThat(clientList.size()).isEqualTo(2);
        assertThat(clientList.getFirst().getId()).isEqualTo(existingClientId_1);
        assertThat(clientList.getFirst().getKey()).isEqualTo(existingClientKey_1);
        assertThat(clientList.getFirst().getDescription()).isEqualTo(existingClientDescription_1);

        assertThat(clientList.get(1).getId()).isEqualTo(existingClientId_2);
        assertThat(clientList.get(1).getKey()).isEqualTo(existingClientKey_2);
        assertThat(clientList.get(1).getDescription()).isEqualTo(existingClientDescription_2);
    }

    @Test
    void findByDescription_non_exist() {
        // Arrange by setUp
        // Act: Try to fetch the user created during the setUp() phase
        List<Client> clientList = clientRepository.findByDescription(missingClientDescription);
        // Assert
        assertThat(clientList).isNotNull();
        assertThat(clientList).isEmpty();
    }

    @Test
    void findKeyById_exist() {
        // Arrange by setUp
        // Act
        Optional<String> foundKey_1 = clientRepository.findKeyById(existingClientId_1);
        Optional<String> foundKey_2 = clientRepository.findKeyById(existingClientId_2);

        // Assert
        assertThat(foundKey_1).isPresent();
        assertThat(foundKey_1.get()).isEqualTo(existingClientKey_1);

        assertThat(foundKey_2).isPresent();
        assertThat(foundKey_2.get()).isEqualTo(existingClientKey_2);
    }

    @Test
    void findKeyById_non_exist() {
        // Arrange by setUp
        // Act
        Optional<String> foundKey = clientRepository.findKeyById(missingClientId);

        // Assert
        assertThat(foundKey).isEmpty();
    }

    @Test
    void findDescriptionById_exist() {
        // Arrange by setUp
        // Act
        Optional<String> foundDescription_1 = clientRepository.findDescriptionById(existingClientId_1);
        Optional<String> foundDescription_2 = clientRepository.findDescriptionById(existingClientId_2);

        // Assert
        assertThat(foundDescription_1).isPresent();
        assertThat(foundDescription_1.get()).isEqualTo(existingClientDescription_1);

        assertThat(foundDescription_2).isPresent();
        assertThat(foundDescription_2.get()).isEqualTo(existingClientDescription_2);
    }

    @Test
    void findDescriptionById_non_exist() {
        // Arrange by setUp
        // Act
        Optional<String> foundDescription = clientRepository.findDescriptionById(missingClientId);

        // Assert
        assertThat(foundDescription).isEmpty();
    }

}