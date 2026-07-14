package com.dreamtech.clientresourceaccessapi.controller;

import com.dreamtech.clientresourceaccessapi.dto.*;
import com.dreamtech.clientresourceaccessapi.service.ClientResourceAccessService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/client-resource-access")
public class ClientResourceAccessController {

    private final ClientResourceAccessService service;

    public ClientResourceAccessController(ClientResourceAccessService service) { this.service = service; }

    // 1.1 GET:  Fetch whole object by composite Names;   Matches /api/v1/client-resource-access/client/{clientId}/resource/{resourceId}
    //      curl -v http://www.dreamtechproduct.com/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}
    // GET request: the URL asks for details of a ClientResourceAccess with composite client={clientId} and resource={resourceId}
    @GetMapping("/client/{clientId}/resource/{resourceId}")
    public ResponseEntity<ClientResourceAccessResponse> getByIds(
            @PathVariable Long clientId,
            @PathVariable Long resourceId
    ) {
            ClientResourceAccessResponse response = service.getByIds(clientId, resourceId);
            return ResponseEntity.ok(response);
    }

    // 1.2 DELETE: Remove a record using composite path parameters, Matches /api/v1/client-resource-access/client/{clientId}/resource/{resourceId}
    //      curl -X DELETE http://www.dreamtechproduct.com/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}
    // DELETE request: the URL asks for deleting a ClientResourceAccess with composite client={clientId} and resource={resourceId}
    @DeleteMapping("/client/{clientId}/resource/{resourceId}")
    public ResponseEntity<Void> deleteByIds(
            @PathVariable Long clientId,
            @PathVariable Long resourceId
    ) {
        service.deleteByIds(clientId, resourceId);
        return ResponseEntity.noContent().build();
    }

    // 1.3 GET: Fetch only the description;   Matches /api/v1/client-resource-access/client/{clientId}/resource/{resourceId}/access-code
    //      curl -v http://www.dreamtechproduct.com/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}/access-code
    // GET request: the URL asks for details of a ClientResourceAccess with composite client={clientId} and resource={resourceId}
    @GetMapping("/client/{clientId}/resource/{resourceId}/access-code")
    public ResponseEntity<String> getAccessCodeByIds(
            @PathVariable Long clientId,
            @PathVariable Long resourceId
    ) {
        String accessCode = service.getAccessCodeByIds(clientId, resourceId);
        return ResponseEntity.ok(accessCode);
    }

    // 1.4 GET: Fetch only the description;   Matches /api/v1/client-resource-access/client/{clientId}/resource/{resourceId}/description
    //      curl -v http://www.dreamtechproduct.com/api/v1/client-resource-access/client/{clientId}/resource/{resourceId}/description
    // GET request: the URL asks for details of a ClientResourceAccess with composite client={clientId} and resource={resourceId}
    @GetMapping("/client/{clientId}/resource/{resourceId}/description")
    public ResponseEntity<String> getDescriptionByIds(
            @PathVariable Long clientId,
            @PathVariable Long resourceId
    ) {
        String description = service.getDescriptionByIds(clientId, resourceId);
        return ResponseEntity.ok(description);
    }


    // 2.1 QUERY PARAMETERS: Matches
    //                  /api/v1/client-resource-access/search
    //                  /api/v1/client-resource-access/search?access-code={accessCode}
    //                  /api/v1/client-resource-access/search?description={description}
    //                  /api/v1/client-resource-access/search?access-code={accessCode}&description={description}
    //
    // QUERY PARAMETERS: Matches /api/v1/client-resource-access/search?access-code={accessCode}&description={description}
    //      curl -v http://www.dreamtechproduct.com/api/v1/client-resource-access/search?access-code={accessCode}&description={description}
    // GET request: the URL asks for a ClientResourceAccess with composite access-code={accessCode}&description={description}
    @GetMapping("/search")
    public ResponseEntity<List<ClientResourceAccessResponse>> searchClientResourceAccesses(
            @RequestParam(name = "access-code", required = false) String accessCode,
            @RequestParam(name = "description", required = false) String description

    ) {
        List<ClientResourceAccessResponse> responseList  = service.searchClientResourceAccesses(accessCode, description);
        return ResponseEntity.ok(responseList);
    }



    // 3.1 REQUEST BODY: Matches POST /api/v1/client-resource-access with complex data payloads (usually JSON) for creating resources
    // curl -X POST -d '&client={clientId}&resource={resourceId}&accessCode={accessCode}&description={description}' http://www.dreamtechproduct.com/api/v1/client-resource-access
    // POST request: add a ClientResourceAccess record with enclosed data in the body of HTTP request
    @PostMapping
    public ResponseEntity<ClientResourceAccessResponse> postRequest(
            @Valid @RequestBody ClientResourceAccessPostRequest payLoad
    ) {
        ClientResourceAccessResponse response = service.createClientResourceAccess(payLoad);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    // 4.1 REQUEST BODY: Matches PUT /api/v1/client-resource-access with complex data payloads (usually JSON) for updating resources
    // curl -X PUT -d '&client={clientId}&resource={resourceId}&accessCode={accessCode}&description={description}' http://www.dreamtechproduct.com/api/v1/client-resource-access
    // POST request: add a ClientResourceAccess record with enclosed data in the body of HTTP request
    @PutMapping("/client/{clientId}/resource/{resourceId}")
    public ResponseEntity<ClientResourceAccessResponse> putClient(
            @PathVariable("clientId") Long clientId,
            @PathVariable("resourceId") Long resourceId,
            @Valid @RequestBody ClientResourceAccessPutRequest payLoad
    ) {
        ClientResourceAccessResponse response = service.replaceClientResourceAccess(clientId, resourceId, payLoad);
        return ResponseEntity.ok(response);
    }

    // 4.2 REQUEST BODY: Matches PUT /api/v1/client-resource-access with complex data payloads (usually JSON) for updating resources
    // curl -X PATCH -d '&client={clientId}&resource={resourceId}&accessCode={accessCode}&description={description}' http://www.dreamtechproduct.com/api/v1/client-resource-access
    // POST request: add a ClientResourceAccess record with enclosed data in the body of HTTP request
    @PatchMapping("/client/{clientId}/resource/{resourceId}")
    public ResponseEntity<ClientResourceAccessResponse> patchClient(
            @PathVariable("clientId") Long clientId,
            @PathVariable("resourceId") Long resourceId,
            @Valid @RequestBody ClientResourceAccessPatchRequest payLoad
    ) {
        ClientResourceAccessResponse response = service.updateClientResourceAccess(clientId, resourceId, payLoad);
        return ResponseEntity.ok(response);
    }

}
