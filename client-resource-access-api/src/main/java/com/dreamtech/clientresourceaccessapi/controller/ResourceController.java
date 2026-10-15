package com.dreamtech.clientresourceaccessapi.controller;

import com.dreamtech.clientresourceaccessapi.dto.*;
import com.dreamtech.clientresourceaccessapi.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor // Automatically injects ResourceRepository via constructor
@RequestMapping("/api/v1/resource")
public class ResourceController {

    private final ResourceService resourceService;

    // 1.1 PATH VARIABLE: Matches /api/v1/resource/{id}
    //      curl -v http://www.dreamtechproduct.com/api/v1/resource/{id}
    // GET request: the URL resource asks for details of a resource with id={id}
    @GetMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Get Resource by ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resource successfully retrieved",
                    content = @Content(schema = @Schema(implementation = ResourceResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ResourceResponse> getResourceById(
            @PathVariable("id") Long resourceId
    ) {
        ResourceResponse response = resourceService.getResourceById(resourceId);
        return ResponseEntity.ok(response);
    }

    // 1.2 PATH VARIABLE: Matches /api/v1/resource/{id}
    //      curl -X DELETE http://www.dreamtechproduct.com/api/v1/resource/{id}
    // DELETE request: the URL asks deleting a resource with id={id}
    @DeleteMapping(
            value = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Delete Resource by ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Resource successfully deleted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> deleteResourceById(
            @PathVariable("id") Long resourceId
    ) {
        resourceService.deleteResourceById(resourceId);
        return ResponseEntity.noContent().build();
    }

    // 1.3 QUERY PARAMETERS: Matches /api/v1/resource/{id}/key
    //      curl -v http://www.dreamtechproduct.com/api/v1/resource/{id}/key
    // GET request: the URL asks for key of a resource with id={id}
    @GetMapping(
            value = "/{id}/key",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Get Resource Key by ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resource Key successfully retrieved",
                    content = @Content(schema = @Schema(implementation = TextResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<TextResponse> getResourceKeyById(
            @PathVariable("id") Long resourceId
    ) {
        String key = resourceService.getKeyById(resourceId);
        return ResponseEntity.ok(new TextResponse(key));
    }

    // 1.4 QUERY PARAMETERS: Matches /api/v1/resource/{id}/type
    //      curl -v http://www.dreamtechproduct.com/api/v1/resource/{id}/type
    // GET request: the URL asks for type of a resource with id={id}
    @GetMapping(
            value = "/{id}/type",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Get Resource Type by ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resource Type successfully retrieved",
                    content = @Content(schema = @Schema(implementation = TextResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<TextResponse> getResourceTypeById(
            @PathVariable("id") Long resourceId
    ) {
        String key = resourceService.getTypeById(resourceId);
        return ResponseEntity.ok(new TextResponse(key));
    }

    // 1.5 QUERY PARAMETERS: Matches /api/v1/resource/{id}/description
    //      curl -v http://www.dreamtechproduct.com/api/v1/resource/{id}/description
    // GET request: the URL asks for description of a resource with id={id}
    @GetMapping(
            value = "/{id}/description",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Get Resource Description by ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resource Description successfully retrieved",
                    content = @Content(schema = @Schema(implementation = TextResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource Description not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<TextResponse> getResourceDescriptionById(
            @PathVariable("id") Long resourceId
    ) {
        String key = resourceService.getDescriptionById(resourceId);
        return ResponseEntity.ok(new TextResponse(key));
    }

    // 2.1 QUERY PARAMETERS: Matches
    //                  /api/v1/resource/search
    //                  /api/v1/resource/search?key={key}
    //                  /api/v1/resource/search?type={type}
    //                  /api/v1/resource/search?description={description}
    //                  /api/v1/resource/search?key={key}&type={type}
    //                  /api/v1/resource/search?key={key}&description={description}
    //                  /api/v1/resource/search?type={type}&description={description}
    //                  /api/v1/resource/search?key={key}&type={type}&description={description}
    //      curl -v http://www.dreamtechproduct.com/api/v1/resource/search
    //      curl -v http://www.dreamtechproduct.com/api/v1/resource/search?key={key}
    //      curl -v http://www.dreamtechproduct.com/api/v1/resource/search?type={type}
    //      curl -v http://www.dreamtechproduct.com/api/v1/resource/search?description={description}
    //      curl -v http://www.dreamtechproduct.com/api/v1/resource/search?key={key}&type={type}
    //      curl -v http://www.dreamtechproduct.com/api/v1/resource/search?key={key}&description={description}
    //      curl -v http://www.dreamtechproduct.com/api/v1/resource/search?type={type}&description={description}
    //      curl -v http://www.dreamtechproduct.com/api/v1/resource/search?key={key}&type={type}&description={description}
    // GET request: the URL resource asks for all resources with key={key}
    @GetMapping(
            value = "/search",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Get Resource (Key,Type,Description) by ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resource (Key,Type,Description) successfully retrieved",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = ResourceResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource (Key,Type,Description) not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<ResourceResponse>> searchResources(
            @RequestParam(required = false, name = "key") String resourceKey,
            @RequestParam(required = false, name = "type") String resourceType,
            @RequestParam(required = false, name = "description") String resourceDescription
    ) {
        List<ResourceResponse> responseList = resourceService.searchResources(resourceKey, resourceType, resourceDescription);
        return ResponseEntity.ok(responseList);
    }


    // 3.1 REQUEST BODY: Matches POST /api/v1/resource with complex data payloads (usually JSON) for creating resources
    // curl -X POST -d '&key={key}&type={type}&description={description}' http://www.dreamtechproduct.com/api/v1/resource
    // POST request: add a resource record with enclosed data in the body of HTTP request
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Create Resource")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Resource successfully created",
                    content = @Content(schema = @Schema(implementation = ResourceResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Resource already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ResourceResponse> postRequest(
            @Valid @RequestBody ResourcePostRequest payLoad
    ) {
        ResourceResponse response = resourceService.createResource(payLoad);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    // 4.1 REQUEST BODY: Matches PUT /api/v1/resource?id={id} with complex data payloads (usually JSON)
    //  for updating only the fields provided in the request body payload.
    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Replace Resource")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resource successfully replaced",
                    content = @Content(schema = @Schema(implementation = ResourceResponse.class))
            )
    })
    public ResponseEntity<ResourceResponse> putRequest(
            @PathVariable("id") Long resourceId,
            @Valid @RequestBody ResourcePutRequest payLoad
    ) {
        ResourceResponse response = resourceService.replaceResource(resourceId, payLoad);
        return ResponseEntity.ok(response);
    }

    // 4.2 REQUEST BODY: Matches PATCH /api/v1/resource/{id} with complex data payloads (usually JSON)
    //  for updating only the fields provided in the request body payload.
    @PatchMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Update Resource")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resource successfully updated",
                    content = @Content(schema = @Schema(implementation = ResourceResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ResourceResponse> patchRequest(
            @PathVariable("id") Long resourceId,
            @Valid @RequestBody ResourcePatchRequest payLoad
    ) {
        ResourceResponse response = resourceService.updateResource(resourceId, payLoad);
        return ResponseEntity.ok(response);
    }

}
