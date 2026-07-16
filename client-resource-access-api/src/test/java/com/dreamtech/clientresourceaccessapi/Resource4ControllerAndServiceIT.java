package com.dreamtech.clientresourceaccessapi;

import com.dreamtech.clientresourceaccessapi.dto.ResourcePostRequest;
import com.dreamtech.clientresourceaccessapi.model.Resource;
import com.dreamtech.clientresourceaccessapi.repository.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
        import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
        import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

        import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@SpringBootTest(properties = "spring.main.lazy-initialization=true")
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("h2mem-test") // This tells Spring to load application-h2mem-test.yml
public class Resource4ControllerAndServiceIT {
    /*  Integration Test for ResourceController and ResourceService.
        Only ResourceController (REAL) and ResourceService (REAL) are integrated,
        But ResourceRepository (MOCKITO MOCK) is mocked separately.
        Note: More test cases may be added later.   */

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Converts Java objects to JSON

    /*
    @Autowired // Inject the REAL service
    private ResourceService resourceService;
    */

    @Autowired
    private Environment environment;

    @MockitoBean
    // Mocks the database repository completely by replacing the Spring bean for ResourceRepository with Mockito mock
    private ResourceRepository resourceRepository;


    @Test
    void checkActiveProfile() {
        // This will print the active profiles (e.g., [test]) helping you match the YAML suffix
        //System.out.println("Active profile: " + Arrays.toString(environment.getActiveProfiles()));
        String[] env = environment.getActiveProfiles();
        assertThat(Arrays.toString(env)).isEqualTo("[h2mem-test]");
    }


    @Test
    void getResourceById_normal() throws Exception {
        // Arrange:
        Long mockResourceId = 123456L;

        Resource mockResource = new Resource();
        mockResource.setId(mockResourceId);
        mockResource.setKey("RESOURCE-KEY");
        //mockResource.setType("RESOURCE-TYPE");
        mockResource.setDescription("RESOURCE-DESCRIPTION");
        when(resourceRepository.findById(mockResourceId)).thenReturn(Optional.of(mockResource));

        // Act & Assert:
        mockMvc.perform(get("/api/v1/resource/{id}", mockResourceId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockResource.getId()))
                .andExpect(jsonPath("$.key").value(mockResource.getKey()))
                .andExpect(jsonPath("$.type").value(mockResource.getType()))
                .andExpect(jsonPath("$.description").value(mockResource.getDescription()));
    }

    @Test
    void getResourceById_exception() throws Exception {
        // Arrange:
        Long mockResourceId = 123456L;
        String exceptionMsg = "Resource not found with id: " + mockResourceId;
        when(resourceRepository.findById(mockResourceId)).thenReturn(Optional.empty());

        // Act & Assert:
        mockMvc.perform(get("/api/v1/resource/{id}", mockResourceId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                //.andExpect(jsonPath("$.detail").value(exceptionMsg));
                .andExpect(result -> {
                    // 1. Grab the literal Java exception that was thrown inside the service layer
                    Exception thrownException = result.getResolvedException();
                    // 2. Make sure it isn't null
                    assertNotNull(thrownException, "An exception should have been thrown!");
                    // 3. Match the exact error string you wrote in your orElseThrow clause!
                    assertEquals(exceptionMsg, thrownException.getMessage());
                });
    }

    @Test
    void deleteResourceById_normal() throws Exception {
        // Given:
        Long mockResourceId = 123456L;

        Resource mockResource = new Resource();
        mockResource.setId(mockResourceId);
        mockResource.setKey("RESOURCE-KEY");
        mockResource.setType("RESOURCE-TYPE");
        mockResource.setDescription("RESOURCE-DESCRIPTION");

        when(resourceRepository.findById(mockResourceId)).thenReturn(Optional.of(mockResource));
        Mockito.doNothing().when(resourceRepository).deleteById(mockResourceId);

        // When & Then:
        mockMvc.perform(delete("/api/v1/resource/{id}", mockResourceId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteResourceById_exception() throws Exception {
        // Given:
        Long mockResourceId = 123456L;
        String exceptionMsg = "Resource not found with id: " + mockResourceId;
        when(resourceRepository.findById(mockResourceId)).thenReturn(Optional.empty());

        // When & Then:
        mockMvc.perform(delete("/api/v1/resource/{id}", mockResourceId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    Exception thrownException = result.getResolvedException();
                    assertNotNull(thrownException, "An exception should have been thrown!");
                    assertEquals(exceptionMsg, thrownException.getMessage());
                });

        verify(resourceRepository, never()).deleteById(mockResourceId);
    }


    @Test
    void getResourceKeyById_normal() throws Exception {
        // Arrange:
        Long mockResourceId = 123L;
        String mockedKey = "Resource-Key";
        when(resourceRepository.findKeyById(mockResourceId)).thenReturn(Optional.of(mockedKey));

        // Act & Assert:
        mockMvc.perform(get("/api/v1/resource/{id}/key", mockResourceId))
                .andExpect(status().isOk())
                .andExpect(content().string(mockedKey));
    }

    @Test
    void getResourceKeyById_exception() throws Exception {
        // Given:
        Long resourceId = 579L;
        String exceptionMsg = "Resource not found with id: " + resourceId;
        when(resourceRepository.findKeyById(resourceId)).thenReturn(Optional.empty());

        // When & Then:
        mockMvc.perform(get("/api/v1/resource/{id}/key", resourceId))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    Exception thrownException = result.getResolvedException();
                    assertNotNull(thrownException, "An exception should have been thrown!");
                    assertEquals(exceptionMsg, thrownException.getMessage());
                });
    }

    @Test
    void getResourceTypeById_normal() throws Exception {
        // Arrange:
        Long mockResourceId = 123L;
        String mockedType = "Resource-Type";
        when(resourceRepository.findTypeById(mockResourceId)).thenReturn(Optional.of(mockedType));

        // Act & Assert:
        mockMvc.perform(get("/api/v1/resource/{id}/type", mockResourceId))
                .andExpect(status().isOk())
                .andExpect(content().string(mockedType));
    }

    @Test
    void getResourceTypeById_exception() throws Exception {
        // Given:
        Long resourceId = 579L;
        String exceptionMsg = "Resource not found with id: " + resourceId;
        when(resourceRepository.findKeyById(resourceId)).thenReturn(Optional.empty());

        // When & Then:
        mockMvc.perform(get("/api/v1/resource/{id}/type", resourceId))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    Exception thrownException = result.getResolvedException();
                    assertNotNull(thrownException, "An exception should have been thrown!");
                    assertEquals(exceptionMsg, thrownException.getMessage());
                });
    }


    @Test
    void getResourceDescriptionById_normal() throws Exception {
        // Arrange:
        Long mockResourceId = 123L;
        String mockedDescription = "Resource-Description";
        when(resourceRepository.findDescriptionById(mockResourceId)).thenReturn(Optional.of(mockedDescription));

        // Act & Assert:
        mockMvc.perform(get("/api/v1/resource/{id}/description", mockResourceId))
                .andExpect(status().isOk())
                .andExpect(content().string(mockedDescription));
    }

    @Test
    void getResourceDescriptionById_exception() throws Exception {
        // Given:
        Long resourceId = 579L;
        String exceptionMsg = "Resource not found with id: " + resourceId;
        when(resourceRepository.findDescriptionById(resourceId)).thenReturn(Optional.empty());

        // When & Then: Query parameters in MockMvc are passed as Strings
        mockMvc.perform(get("/api/v1/resource/{id}/description", resourceId))
                .andExpect(status().isNotFound())
                //.andExpect(jsonPath("$.detail").value(exceptionMsg));
                .andExpect(result -> {
                    // 1. Grab the literal Java exception that was thrown inside the service layer
                    Exception thrownException = result.getResolvedException();
                    // 2. Make sure it isn't null
                    assertNotNull(thrownException, "An exception should have been thrown!");
                    // 3. Match the exact error string you wrote in your orElseThrow clause!
                    assertEquals(exceptionMsg, thrownException.getMessage());
                });
    }


    @Test
    void searchResources_key_with_one_response() throws Exception {
        // Arrange:
        String resourceKey = "Resource-Key";
        Resource resource = new Resource();
        resource.setId(123L);
        resource.setKey("123-key");
        resource.setType("123-type");

        when(resourceRepository.findByKey(resourceKey)).thenReturn(List.of(resource));

        // Act & Assert:
        mockMvc.perform(get("/api/v1/resource/search")
                        .param("key", resourceKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(resource.getId()))
                .andExpect(jsonPath("$[0].key").value(resource.getKey()))
                .andExpect(jsonPath("$[0].description").value(resource.getDescription()));

        verify(resourceRepository).findByKey(resourceKey);
    }

    @Test
    void searchResources_type_with_no_response() throws Exception {
        // Arrange:
        String resourceType = "Resource-Type";

        when(resourceRepository.findByType(resourceType)).thenReturn(List.of());

        // Act & Assert:
        mockMvc.perform(get("/api/v1/resource/search")
                        .param("type", resourceType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(resourceRepository).findByType(resourceType);
    }

    @Test
    void searchResources_description_with_two_response() throws Exception {
        // Arrange:
        String resourceDescription = "Resource-Description";

        Resource resource_1 = new Resource();
        resource_1.setId(111111L);
        resource_1.setKey("111111-key");
        resource_1.setType("111111-type");

        Resource resource_2 = new Resource();
        resource_2.setId(22222222L);
        resource_2.setKey("2222222222-key");

        when(resourceRepository.findByDescription(resourceDescription)).thenReturn(List.of(resource_1, resource_2));

        // Act & Assert:
        mockMvc.perform(get("/api/v1/resource/search")
                        .param("description", resourceDescription))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(resource_1.getId()))
                .andExpect(jsonPath("$[0].key").value(resource_1.getKey()))
                .andExpect(jsonPath("$[0].description").value(resource_1.getDescription()))
                .andExpect(jsonPath("$[1].id").value(resource_2.getId()))
                .andExpect(jsonPath("$[1].key").value(resource_2.getKey()))
                .andExpect(jsonPath("$[1].description").isEmpty());

        verify(resourceRepository).findByDescription(resourceDescription);
    }

    @Test
    void searchResources_key_description_with_two_response() throws Exception {
        // Arrange:
        String resourceKey = "RESOURCE-KEY";
        String resourceDescription = "Resource-Description";

        Resource resource_1 = new Resource();
        resource_1.setId(111111L);
        resource_1.setKey("111111-key");

        Resource resource_2 = new Resource();
        resource_2.setId(22222222L);
        resource_2.setKey("2222222222-key");
        resource_2.setType("2222222222-type");

        when(resourceRepository.findByKeyAndDescription(resourceKey, resourceDescription)).thenReturn(List.of(resource_1, resource_2));

        // Act & Assert:
        mockMvc.perform(get("/api/v1/resource/search")
                        .param("key", resourceKey)
                        .param("description", resourceDescription))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(resource_1.getId()))
                .andExpect(jsonPath("$[0].key").value(resource_1.getKey()))
                .andExpect(jsonPath("$[0].description").isEmpty())
                .andExpect(jsonPath("$[1].id").value(resource_2.getId()))
                .andExpect(jsonPath("$[1].key").value(resource_2.getKey()))
                .andExpect(jsonPath("$[1].description").value(resource_2.getDescription()));

        verify(resourceRepository).findByKeyAndDescription(resourceKey, resourceDescription);
    }

    @Test
    void searchResources_key_type_description_with_one_response() throws Exception {
        // Arrange:
        String resourceKey = "Resource-Key";
        String resourceType = "Resource-Type";
        String resourceDescription = "Resource-Description";

        Resource resource = new Resource();
        resource.setId(123L);
        resource.setKey("123-key");
        resource.setType("123-type");

        when(resourceRepository.findByKeyAndTypeAndDescription(resourceKey, resourceType, resourceDescription)).thenReturn(List.of(resource));

        // Act & Assert:
        mockMvc.perform(get("/api/v1/resource/search")
                        .param("key", resourceKey)
                        .param("type", resourceType)
                        .param("description", resourceDescription)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(resource.getId()))
                .andExpect(jsonPath("$[0].key").value(resource.getKey()))
                .andExpect(jsonPath("$[0].type").value(resource.getType()))
                .andExpect(jsonPath("$[0].description").value(resource.getDescription()));

        verify(resourceRepository).findByKeyAndTypeAndDescription(resourceKey, resourceType, resourceDescription);
    }


    @Test
    void postRequest_normal() throws Exception {
        // Arrange:
        ResourcePostRequest request = ResourcePostRequest.builder()
                .key("Key-13579")
                .type("13579-Type")
                .description("Descreption-13579")
                .build();

        String jsonPayload = objectMapper.writeValueAsString(request);

        Resource mockResource = new Resource();
        mockResource.setId(129834765L);
        mockResource.setKey("Resource-Name");
        mockResource.setType("Resource-Url-RESOURCE");
        mockResource.setDescription("Resource Description");

        when(resourceRepository.findByKey(any(String.class))).thenReturn(List.of());
        when(resourceRepository.save(any(Resource.class))).thenReturn(mockResource);

        // Act & Assert:
        mockMvc.perform(post("/api/v1/resource")
                        .contentType(MediaType.APPLICATION_JSON)    // Set Content-Type: application/json
                        .content(jsonPayload))
                .andExpect(status().isCreated())                         // Expect HTTP 200 OK
                .andExpect(jsonPath("$.id").value(mockResource.getId()))
                .andExpect(jsonPath("$.key").value(mockResource.getKey()))
                .andExpect(jsonPath("$.type").value(mockResource.getType()))
                .andExpect(jsonPath("$.description").value(mockResource.getDescription()));
    }

    @Test
    void putRequest_replace_existing() throws Exception {
        // Arrange:
        Long resourceId = 123789L;
        ResourcePostRequest request = ResourcePostRequest.builder()
                .key("KEY-" + resourceId)
                .type(resourceId + "-TYPE")
                .description("This is " + resourceId + " Description")
                .build();

        String jsonPayload = objectMapper.writeValueAsString(request);

        Resource mockResource = new Resource();
        mockResource.setId(resourceId);
        mockResource.setKey("Mocked_Db_" + resourceId + "_Key");
        mockResource.setType(resourceId + "_Mocked_Db_Type");
        mockResource.setDescription("This is mocked Resource " + resourceId + " description");

        when(resourceRepository.findById(any(Long.class))).thenReturn(Optional.of(mockResource));
        when(resourceRepository.save(any(Resource.class))).thenReturn(mockResource);

        // Act & Assert:
        mockMvc.perform(put("/api/v1/resource/{id}", resourceId)
                        .contentType(MediaType.APPLICATION_JSON)        // Set Content-Type: application/json
                        .content(jsonPayload))
                .andExpect(status().isOk())                             // Expect HTTP 200 OK
                .andExpect(jsonPath("$.id").value(mockResource.getId()))
                .andExpect(jsonPath("$.key").value(mockResource.getKey()))
                .andExpect(jsonPath("$.type").value(mockResource.getType()))
                .andExpect(jsonPath("$.description").value(mockResource.getDescription()));
    }

    @Test
    void putRequest_as_new_resource() throws Exception {
        // Arrange:
        java.lang.Long resourceId = 987654321L;
        ResourcePostRequest request = ResourcePostRequest.builder()
                .key("KEY-" + resourceId)
                .type(resourceId + "-TYPE")
                .description(resourceId + "-Description")
                .build();

        String jsonPayload = objectMapper.writeValueAsString(request);

        Resource mockResource = new Resource();
        mockResource.setId(resourceId);
        mockResource.setKey("Mocked_Db_" + resourceId + "_Name");
        mockResource.setType(resourceId + "_Mocked_Db_URL");
        mockResource.setDescription("This is mocked Resource " + resourceId + " description");

        when(resourceRepository.findById(any(Long.class))).thenReturn(Optional.empty());
        when(resourceRepository.save(any(Resource.class))).thenReturn(mockResource);

        // Act & Assert:
        mockMvc.perform(put("/api/v1/resource/{id}", resourceId)
                        .contentType(MediaType.APPLICATION_JSON)        // Set Content-Type: application/json
                        .content(jsonPayload))
                .andExpect(status().isOk())                             // Expect HTTP 200 OK
                .andExpect(jsonPath("$.id").value(mockResource.getId()))
                .andExpect(jsonPath("$.key").value(mockResource.getKey()))
                .andExpect(jsonPath("$.type").value(mockResource.getType()))
                .andExpect(jsonPath("$.description").value(mockResource.getDescription()));
    }


    @Test
    void patchRequest_normal() throws Exception{
        // Arrange:
        Long resourceId = 987654321L;
        ResourcePostRequest request = ResourcePostRequest.builder()
                .key("KEY-" + resourceId)
                .type(resourceId + "-TYPE")
                .description("This is " + resourceId + " Description")
                .build();

        String jsonPayload = objectMapper.writeValueAsString(request);

        Resource mockResource = new Resource();
        mockResource.setId(resourceId);
        mockResource.setKey("Mocked_Db_" + resourceId + "_Key");
        mockResource.setType(resourceId + "_Mocked_Db_Type");
        mockResource.setDescription("This is mocked Resource " + resourceId + " description");

        when(resourceRepository.findById(any(Long.class))).thenReturn(Optional.of(mockResource));
        when(resourceRepository.save(any(Resource.class))).thenReturn(mockResource);

        // Act & Assert:
        mockMvc.perform(patch("/api/v1/resource/{id}", resourceId)
                        .contentType(MediaType.APPLICATION_JSON)        // Set Content-Type: application/json
                        .content(jsonPayload))
                .andExpect(status().isOk())                             // Expect HTTP 200 OK
                .andExpect(jsonPath("$.id").value(mockResource.getId()))
                .andExpect(jsonPath("$.key").value(mockResource.getKey()))
                .andExpect(jsonPath("$.type").value(mockResource.getType()))
                .andExpect(jsonPath("$.description").value(mockResource.getDescription()));
    }

    @Test
    void patchRequest_exception() throws Exception {
        // Arrange:
        Long resourceId = 987654321L;
        String exceptionMsg = "Resource not found with id: " + resourceId;

        ResourcePostRequest request = ResourcePostRequest.builder()
                .key("KEY-" + resourceId)
                .type(resourceId + "-TYPE")
                .description("This is " + resourceId + " Description")
                .build();

        String jsonPayload = objectMapper.writeValueAsString(request);

        when(resourceRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        // Act & Assert:
        mockMvc.perform(patch("/api/v1/resource/{id}", resourceId)
                        .contentType(MediaType.APPLICATION_JSON)        // Set Content-Type: application/json
                        .content(jsonPayload))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    Exception thrownException = result.getResolvedException();
                    assertNotNull(thrownException, "An exception should have been thrown!");
                    assertEquals(exceptionMsg, thrownException.getMessage());
                });
    }

}
