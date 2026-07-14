package com.dreamtech.clientresourceaccessapi.repository;

import com.dreamtech.clientresourceaccessapi.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // Boots up ONLY the JPA components using the H2 in-memory database specified by the test YAML
@ActiveProfiles("h2mem-test") // Loads application-h2mem-test.yml
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Forces Spring to use your YAML parameters
class ResourceRepositoryTest {
    /*  Pure Unit Test.
        Unit test for ResourceRepository using the H2 in-memory database.
        Note: More test cases may be added later.   */

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager; // Inject EntityManager

    @Autowired
    private Environment environment;

    private Long resourceId_1, resourceId_2, resourceId_3;

    private final String RESOURCE_KEY_PATTERN = "RESOURCE_KEY";
    private final String resourceKey_1 = "A1-" + RESOURCE_KEY_PATTERN;
    private final String resourceKey_2 = RESOURCE_KEY_PATTERN + "-B2";
    private final String resourceKey_3 = "C-" + RESOURCE_KEY_PATTERN + "-3";

    private final String RESOURCE_TYPE_PATTERN = "RESOURCE_TYPE";
    private final String resourceType_1 = RESOURCE_TYPE_PATTERN + "MYT-YPE";
    private final String resourceType_2 = "B2B2-" + RESOURCE_TYPE_PATTERN;
    private final String resourceType_3 = "MYT-YPE-" + RESOURCE_TYPE_PATTERN + "-333";

    private final String RESOURCE_DESC_PATTERN = "THIS IS A RESOURCE_DESCRIPTION SAMPLE";
    private final String resourceDesc_1 = "MYDESCRIPTION-" + RESOURCE_DESC_PATTERN;
    private final String resourceDesc_2 = RESOURCE_DESC_PATTERN + "MYDESCRIPTION";


    @BeforeEach
    void setUp() {
        // The real H2 in-memory database is used here.
        resourceRepository.deleteAll();

        // Reset the H2 auto-increment counter for your table
        //entityManager.createNativeQuery("ALTER TABLE enterprise_resource ALTER COLUMN id RESTART WITH 1").executeUpdate();

        Resource resource;

        Resource resource1 = new Resource();    // resource1 has: Key Type Description
        resource1.setKey(resourceKey_1);
        resource1.setType(resourceType_1);
        resource1.setDescription(resourceDesc_1);
        resource = resourceRepository.save(resource1);
        resourceId_1 = resource.getId();

        Resource resource2 = new Resource();    // resource2 has: Key Description
        resource2.setKey(resourceKey_2);
        resource2.setType(resourceType_2);
        resource2.setDescription(resourceDesc_2);
        resource = resourceRepository.save(resource2);
        resourceId_2 = resource.getId();

        Resource resource3 = new Resource();    // resource3 has: Key Type
        resource3.setKey(resourceKey_3);
        resource3.setType(resourceType_3);
        resource = resourceRepository.save(resource3);
        resourceId_3 = resource.getId();

        // Save to H2 and preserve the object (with its auto-generated ID) for verification
        List<Resource> listResource = resourceRepository.findAll();
        listResource.sort(Comparator.comparing(Resource::getId));
    }

    // TEARDOWN NOTE: No explicit @AfterEach is needed!
    // @DataJpaTest automatically rolls back the database transaction after every test.


    @Test
    void checkActiveProfile() {
        // This will print the active profiles (e.g., [test]) helping you match the YAML suffix
        //System.out.println("Active profile: " + Arrays.toString(environment.getActiveProfiles()));
        String[] env = environment.getActiveProfiles();
        assertThat(Arrays.toString(env)).isEqualTo("[h2mem-test]");
    }


    @Test
    void findByKey_with_three_record() {
        // Act: Try to fetch the user created during the setUp() phase
        List<Resource> resourceList = resourceRepository.findByKey(RESOURCE_KEY_PATTERN);

        // Assert
        assertThat(resourceList).isNotEmpty();
        assertThat(resourceList.size()).isEqualTo(3);

        assertThat(resourceList.getFirst().getId()).isGreaterThan(0L);
        assertThat(resourceList.getFirst().getKey()).isEqualTo(resourceKey_1);
        assertThat(resourceList.getFirst().getType()).isEqualTo(resourceType_1);
        assertThat(resourceList.getFirst().getDescription()).isEqualTo(resourceDesc_1);

        assertThat(resourceList.get(1).getId()).isGreaterThan(1L);
        assertThat(resourceList.get(1).getKey()).isEqualTo(resourceKey_2);
        assertThat(resourceList.get(1).getType()).isEqualTo(resourceType_2);
        assertThat(resourceList.get(1).getDescription()).isEqualTo(resourceDesc_2);

        assertThat(resourceList.get(2).getId()).isGreaterThan(2L);
        assertThat(resourceList.get(2).getKey()).isEqualTo(resourceKey_3);
        assertThat(resourceList.get(2).getType()).isEqualTo(resourceType_3);
        assertThat(resourceList.get(2).getDescription()).isNull();
    }

    @Test
    void findByKey_with_no_record() {
        // Act: Try to fetch the user created during the setUp() phase
        List<Resource> resourceList = resourceRepository.findByKey("A1B2C3");

        assertThat(resourceList).isEmpty();
    }


    @Test
    void findByType_with_three_record() {
        List<Resource> resourceList = resourceRepository.findByType(RESOURCE_TYPE_PATTERN);

        assertThat(resourceList).isNotEmpty();
        assertThat(resourceList.size()).isEqualTo(3);

        assertThat(resourceList.getFirst().getKey()).isEqualTo(resourceKey_1);
        assertThat(resourceList.getFirst().getType()).isEqualTo(resourceType_1);
        assertThat(resourceList.getFirst().getDescription()).isEqualTo(resourceDesc_1);

        assertThat(resourceList.get(1).getKey()).isEqualTo(resourceKey_2);
        assertThat(resourceList.get(1).getType()).isEqualTo(resourceType_2);
        assertThat(resourceList.get(1).getDescription()).isEqualTo(resourceDesc_2);

        assertThat(resourceList.get(2).getKey()).isEqualTo(resourceKey_3);
        assertThat(resourceList.get(2).getType()).isEqualTo(resourceType_3);
        assertThat(resourceList.get(2).getDescription()).isNull();
    }

    @Test
    void findByType_with_no_record() {
        // Act: Try to fetch the user created during the setUp() phase
        List<Resource> resourceList = resourceRepository.findByType("A1B2C3");

        assertThat(resourceList).isEmpty();
    }


    @Test
    void findByDescription_with_two_record() {
        List<Resource> resourceList = resourceRepository.findByDescription(RESOURCE_DESC_PATTERN);

        assertThat(resourceList).isNotEmpty();
        assertThat(resourceList.size()).isEqualTo(2);

        assertThat(resourceList.getFirst().getKey()).isEqualTo(resourceKey_1);
        assertThat(resourceList.getFirst().getType()).isEqualTo(resourceType_1);
        assertThat(resourceList.getFirst().getDescription()).isEqualTo(resourceDesc_1);

        assertThat(resourceList.get(1).getKey()).isEqualTo(resourceKey_2);
        assertThat(resourceList.get(1).getType()).isEqualTo(resourceType_2);
        assertThat(resourceList.get(1).getDescription()).isEqualTo(resourceDesc_2);
    }

    @Test
    void findByDescription_with_no_record() {
        // Act: Try to fetch the user created during the setUp() phase
        List<Resource> resourceList = resourceRepository.findByDescription("A1B2C3");

        assertThat(resourceList).isEmpty();
    }


    @Test
    void findByKeyAndType_with_two_record() {
        List<Resource> resourceList = resourceRepository.findByKeyAndType("RESOURCE_KEY", "MYT-YPE");

        assertThat(resourceList).isNotEmpty();
        assertThat(resourceList.size()).isEqualTo(2);
    }

    @Test
    void findByKeyAndDescription_with_two_record() {
        List<Resource> resourceList = resourceRepository.findByKeyAndDescription("RESOURCE_KEY", "MYDESCRIPTION");

        assertThat(resourceList).isNotEmpty();
        assertThat(resourceList.size()).isEqualTo(2);
    }

    @Test
    void findByKeyAndTypeAndDescription_with_one_record() {
        List<Resource> resourceList = resourceRepository.findByKeyAndTypeAndDescription("RESOURCE_KEY", "RESOURCE_TYPE", "RESOURCE_DES");

        assertThat(resourceList).isNotEmpty();
        assertThat(resourceList.size()).isEqualTo(2);

        assertThat(resourceList.getFirst().getKey()).isEqualTo(resourceKey_1);
        assertThat(resourceList.getFirst().getType()).isEqualTo(resourceType_1);
        assertThat(resourceList.getFirst().getDescription()).isEqualTo(resourceDesc_1);

        assertThat(resourceList.get(1).getKey()).isEqualTo(resourceKey_2);
        assertThat(resourceList.get(1).getType()).isEqualTo(resourceType_2);
        assertThat(resourceList.get(1).getDescription()).isEqualTo(resourceDesc_2);
    }


    @Test
    void findKeyById() {
        java.util.Optional<String> optionalKey = resourceRepository.findKeyById(resourceId_1);
        assertThat(optionalKey.orElse(null)).isEqualTo(resourceKey_1);

        optionalKey = resourceRepository.findKeyById(resourceId_2);
        assertThat(optionalKey.orElse(null)).isEqualTo(resourceKey_2);

        optionalKey = resourceRepository.findKeyById(resourceId_3);
        assertThat(optionalKey.orElse(null)).isEqualTo(resourceKey_3);
    }

    @Test
    void findTypeById() {
        java.util.Optional<String> optionalType = resourceRepository.findTypeById(resourceId_1);
        assertThat(optionalType.orElse(null)).isEqualTo(resourceType_1);

        optionalType = resourceRepository.findTypeById(resourceId_2);
        assertThat(optionalType.orElse(null)).isEqualTo(resourceType_2);

        optionalType = resourceRepository.findTypeById(resourceId_3);
        assertThat(optionalType.orElse(null)).isEqualTo(resourceType_3);
    }

    @Test
    void findDescriptionById() {
        java.util.Optional<String> optionalDesc = resourceRepository.findDescriptionById(resourceId_1);
        assertThat(optionalDesc.orElse(null)).isEqualTo(resourceDesc_1);

        optionalDesc = resourceRepository.findDescriptionById(resourceId_2);
        assertThat(optionalDesc.orElse(null)).isEqualTo(resourceDesc_2);

        optionalDesc = resourceRepository.findDescriptionById(resourceId_3);
        assertThat(optionalDesc.orElse(null)).isNull();
    }
}