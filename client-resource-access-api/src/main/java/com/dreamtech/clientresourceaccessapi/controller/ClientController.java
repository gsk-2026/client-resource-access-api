package com.dreamtech.clientresourceaccessapi.controller;

import com.dreamtech.clientresourceaccessapi.dto.*;
import com.dreamtech.clientresourceaccessapi.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/client")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }


    // 1.1 PATH VARIABLE: Matches /api/v1/client/{id}
    //      curl -v http://www.dreamtechproduct.com/api/v1/client/{id}
    // GET request: the URL client asks for details of a client with id={id}
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getClientById(
            @PathVariable("id") Long clientId
    ) {
        ClientResponse response = clientService.getClientById(clientId);
        return ResponseEntity.ok(response);
    }

    // 1.2 PATH VARIABLE: Matches /api/v1/client/{id}
    //      curl -X DELETE http://www.dreamtechproduct.com/api/v1/client/{id}
    // DELETE request: the URL client asks deleting a client with id={id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClientById(
            @PathVariable("id") Long clientId
    ) {
        clientService.deleteClientById(clientId);
        return ResponseEntity.noContent().build();
    }

    // 1.3 QUERY PARAMETERS: Matches /api/v1/client/{id}/key
    //      curl -v http://www.dreamtechproduct.com/api/v1/client/{id}/key
    // GET request: the URL client asks for key of a client with id={id}
    @GetMapping("/{id}/key")
    public ResponseEntity<String> getClientKeyById(
            @PathVariable("id") Long clientId
    ) {
        String key = clientService.getKeyById(clientId);
        return ResponseEntity.ok(key);
    }

    // 1.4 QUERY PARAMETERS: Matches /api/v1/client/{id}/description
    //      curl -v http://www.dreamtechproduct.com/api/v1/client/{id}/description
    // GET request: the URL client asks for description of a client with id={id}
    @GetMapping("/{id}/description")
    public ResponseEntity<String> getClientDescriptionById(
            @PathVariable("id") Long clientId
    ) {
        String description = clientService.getDescriptionById(clientId);
        return ResponseEntity.ok(description);
    }


    // 2.1 QUERY PARAMETERS: Matches
    //                  /api/v1/client/search
    //                  /api/v1/client/search?key={key}
    //                  /api/v1/client/search?description={description}
    //                  /api/v1/client/search?key={key}&description={description}
    //      curl -v http://www.dreamtechproduct.com/api/v1/client/search
    //      curl -v http://www.dreamtechproduct.com/api/v1/client/search?key={key}
    //      curl -v http://www.dreamtechproduct.com/api/v1/client/search?description={description}
    //      curl -v http://www.dreamtechproduct.com/api/v1/client/search?key={key}&description={description}
    // GET request: the URL client asks for all clients with key={key}&description={description}
    @GetMapping("/search")
    public ResponseEntity<List<ClientResponse>> searchClients(
            @RequestParam(required = false, name = "key") String clientKey,
            @RequestParam(required = false, name = "description") String clientDescription
    ) {
        List<ClientResponse> listResponse = clientService.searchClients(clientKey, clientDescription);
        return ResponseEntity.ok(listResponse);
    }


    // 3.1 REQUEST BODY: Matches POST /api/v1/client with complex data payloads (usually JSON) for creating resources
    // curl -X POST -d 'key={key}&description={description}' http://www.dreamtechproduct.com/api/v1/client
    // POST request: add a client record with enclosed data in the body of HTTP request
    @PostMapping
    public ResponseEntity<ClientResponse> postRequest(
            @Valid @RequestBody ClientPostRequest payload
    ) {
        ClientResponse response = clientService.createClient(payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    // 4.1 REQUEST BODY: Matches PUT /api/v1/client/{id} with complex data payloads (usually JSON)
    //  for updating only the fields provided in the request body payload.
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> putRequest(
            @PathVariable("id") Long clientId,
            @Valid @RequestBody ClientPutRequest payLoad
    ) {
        ClientResponse response = clientService.replaceClient(clientId, payLoad);
        return ResponseEntity.ok(response);
    }

    // 4.2 REQUEST BODY: Matches PATCH /api/v1/client/{id} with complex data payloads (usually JSON)
    //  for updating only the fields provided in the request body payload.
    @PatchMapping("/{id}")
    public ResponseEntity<ClientResponse> patchRequest(
            @PathVariable("id") Long clientId,
            @Valid @RequestBody ClientPatchRequest payLoad
    ) {
        ClientResponse response = clientService.updateClient(clientId, payLoad);
        return ResponseEntity.ok(response);
    }

    // 4.3 REQUEST BODY: Matches PATCH /api/v1/client/{id}/secret with complex data payloads (usually JSON)
    //  for updating only the fields provided in the request body payload.
    @PatchMapping("/{id}/secret")
    public ResponseEntity<Void> patchSecretRequest(
            @PathVariable("id") Long clientId,
            @Valid @RequestBody ClientPatchSecretRequest payLoad) {
        clientService.updateSecret(clientId, payLoad);
        return ResponseEntity.noContent().build();
    }


    // 5. REQUEST HEADER: Extracts metadata from the HTTP request headers
    //      TODO: Authentication and Authorization: Scoped out of the current phase; to be addressed later
    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> authService(
            // 1. Extract a required specific header
            @RequestHeader(value = "Authorization") String authToken,
            // 2. Extract an optional specific header with a default fallback
            @RequestHeader(value = "X-Custom-Client-Id", required = false) String clientId,
            // 3. Extract all headers at once into a map
            @RequestHeader Map<String, String> allHeaders) {

        //Map<String, Object> response = new HashMap<>();
        Map<String, Object> response = allHeaders.entrySet().stream()
                .filter(entry -> !entry.getKey().equalsIgnoreCase("password")) // Filter out sensitive data
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        response.put("AuthorizationToken", authToken);
        if (clientId != null) { response.put("XCustomClientID", clientId); }

        // Iterate through all headers
        //allHeaders.forEach((key, value) -> { response.put(key, value); });

        return ResponseEntity.ok(response);
    }
}
