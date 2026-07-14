package com.dreamtech.clientresourceaccessapi;


import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;


public class ClientResourceAccessApiE2ELoadScenario extends Simulation {
/*
    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080/api/v1")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");
*/
    private static final String BASE_URL =
            System.getProperty(
                    "gatling.baseUrl",
                    "http://localhost:8080/api/v1");

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .disableWarmUp();

    private static int getIntConfig(String key, int defaultValue) {
        return Optional.ofNullable(System.getenv(key))
                .or(() -> Optional.ofNullable(System.getProperty(key)))
                .map(Integer::parseInt)
                .orElse(defaultValue);
    }

    private final int loops = getIntConfig("LOOPS", 50);
    private final Duration pauseTime = Duration.ofSeconds(getIntConfig("PAUSE_SECONDS", 5));


    /***   CLIENT
     *     User session for Http Actions (POST PUT PATCH GET DELETE) is isolated   ***/

    private final Iterator<Map<String, Object>> clientFeeder =
            Stream.generate(() -> Collections.singletonMap("uniqueClientMark", (Object) UUID.randomUUID().toString().substring(0, 8)))
                    .iterator();

    private final ChainBuilder clientCrudLifecycle = exec(
            // Feed a unique token string to this specific isolated user session
            feed(clientFeeder),

            // 1001. POST - Create its own dedicated record
            http("1001. POST - Create Client")
                    .post("/client")
                    .body(StringBody("{\"key\": \"Key-#{uniqueClientMark}\", \"description\": \"Desc-#{uniqueClientMark}\"}"))
                    .check(status().is(201))
                    // CAPTURE: Saves the database generated primary key into this user's PRIVATE session
                    .check(jsonPath("$.id").saveAs("myClientId"))
                    .check(jsonPath("$.key").isEL("Key-#{uniqueClientMark}"))
                    .check(jsonPath("$.description").isEL("Desc-#{uniqueClientMark}")),

            pause(pauseTime),

            // 1002. GET - Fetch only its own record
            http("1002. GET - Fetch Client")
                    .get("/client/#{myClientId}")
                    .check(status().is(200))
                    .check(jsonPath("$.key").isEL("Key-#{uniqueClientMark}"))
                    .check(jsonPath("$.description").isEL("Desc-#{uniqueClientMark}")),

            pause(pauseTime),

            // 1003. GET - Fetch the Key
            http("1003. GET - Fetch Client - Key")
                    .get("/client/#{myClientId}/key")
                    .check(status().is(200))
                    .check(bodyString().isEL("Key-#{uniqueClientMark}")),

            pause(pauseTime),

            // 1004. GET - Fetch the Key
            http("1004. GET - Fetch Client - Description")
                    .get("/client/#{myClientId}/description")
                    .check(status().is(200))
                    .check(bodyString().isEL("Desc-#{uniqueClientMark}")),

            pause(pauseTime),

            // 1005. PUT - Update only its own record
            http("1005. PUT - Replace Client")
                    .put("/client/#{myClientId}")
                    .body(StringBody("{\"key\": \"Key-#{uniqueClientMark}\", \"description\": \"Replaced-#{uniqueClientMark}\"}"))
                    .check(status().is(200))
                    .check(jsonPath("$.key").isEL("Key-#{uniqueClientMark}"))
                    .check(jsonPath("$.description").isEL("Replaced-#{uniqueClientMark}")),

            pause(pauseTime),

            // 1006. PATCH - Patch only its own record
            http("1006. PATCH - Update Client")
                    .patch("/client/#{myClientId}")
                    .body(StringBody("{\"key\": \"Key-#{uniqueClientMark}-Patched\", \"description\": \"Patched-#{uniqueClientMark}\"}"))
                    .check(status().is(200))
                    .check(jsonPath("$.key").isEL("Key-#{uniqueClientMark}-Patched"))
                    .check(jsonPath("$.description").isEL("Patched-#{uniqueClientMark}")),

            pause(pauseTime),

            // 1007. DELETE - Destroy its own record when completely finished
            http("1007. DELETE - Remove Client")
                    .delete("/client/#{myClientId}")
                    // Match against your controller's exact deletion response code (200 or 204)
                    .check(status().in(200, 204))
    );

    // Scenario structure forcing absolute isolation rules
    private final ScenarioBuilder clientCrudScenario = scenario("Isolated User CRUD Cycles - Client")
            // Each user executes the complete sequence exactly 1 time from top to bottom
            .repeat(1).on(clientCrudLifecycle);



    /***   RESOURCE
     *     User sessions for Http Actions (POST PUT PATCH GET DELETE) are in parallel   ***/

    private final Iterator<Map<String, Object>> resourceFeeder =
            Stream.generate(() -> Collections.singletonMap("uniqueResourceMark", (Object) UUID.randomUUID().toString().substring(0, 8)))
                    .iterator();

    private final ScenarioBuilder resourceCrudScenario = scenario("Parallel User CRUD Cycles - Resource")
            // User sessions are in parallel
            .feed(resourceFeeder)

            // 2001. POST - Create Resource
            .exec(http("2001. POST - Create Resource")
                    .post("/resource")
                    .body(StringBody("{\"key\": \"Resource-Key-#{uniqueResourceMark}\"," +
                            "\"type\": \"Resource-Type-#{uniqueResourceMark}\"," +
                            "\"description\": \"Resource-Description-#{uniqueResourceMark}\"}"))
                    .check(status().is(201))
                    .check(jsonPath("$.id").saveAs("myResourceId"))
                    .check(jsonPath("$.key").isEL("Resource-Key-#{uniqueResourceMark}"))
                    .check(jsonPath("$.type").isEL("Resource-Type-#{uniqueResourceMark}"))
                    .check(jsonPath("$.description").isEL("Resource-Description-#{uniqueResourceMark}")))
            .pause(pauseTime)

            // 2002. GET - Fetch Resource
            .exec(http("2002. GET - Fetch Resource")
                    .get("/resource/#{myResourceId}")
                    .check(status().is(200))
                    .check(jsonPath("$.key").isEL("Resource-Key-#{uniqueResourceMark}"))
                    .check(jsonPath("$.type").isEL("Resource-Type-#{uniqueResourceMark}"))
                    .check(jsonPath("$.description").isEL("Resource-Description-#{uniqueResourceMark}")))
            .pause(pauseTime)

            // 2003. GET - Fetch Resource Key
            .exec(http("2003. GET - Fetch Resource - Key")
                    .get("/resource/#{myResourceId}/key")
                    .check(status().is(200))
                    .check(bodyString().isEL("Resource-Key-#{uniqueResourceMark}")))
            .pause(pauseTime)

            // 2004. GET - Fetch Resource Type
            .exec(http("2004. GET - Fetch Resource - Type")
                    .get("/resource/#{myResourceId}/type")
                    .check(status().is(200))
                    .check(bodyString().isEL("Resource-Type-#{uniqueResourceMark}")))
            .pause(pauseTime)

            // 2005. GET - Fetch Resource Description
            .exec(http("2005. GET - Fetch Resource - Description")
                    .get("/resource/#{myResourceId}/description")
                    .check(status().is(200))
                    .check(bodyString().isEL("Resource-Description-#{uniqueResourceMark}")))
            .pause(pauseTime)

            // 2006. PUT - Update Resource
            .exec(http("2006. PUT - Replace Resource")
                    .put("/resource/#{myResourceId}")
                    .body(StringBody("{\"key\": \"Resource-Key-#{uniqueResourceMark}-Replaced\"," +
                            "\"type\": \"#{uniqueResourceMark}-Replaced-Resourced-Type\"," +
                            "\"description\": \"Resource-Description-Replaced-#{uniqueResourceMark}\"}"))
                    .check(status().is(200))
                    .check(jsonPath("$.key").isEL("Resource-Key-#{uniqueResourceMark}-Replaced"))
                    .check(jsonPath("$.type").isEL("#{uniqueResourceMark}-Replaced-Resourced-Type"))
                    .check(jsonPath("$.description").isEL("Resource-Description-Replaced-#{uniqueResourceMark}")))
            .pause(pauseTime)

            // 2007. PATCH - Update Resource
            .exec(http("2007. PATCH - Update Resource")
                    .patch("/resource/#{myResourceId}")
                    .body(StringBody("{\"key\": \"Updated-Resource-Key-#{uniqueResourceMark}\"," +
                            "\"type\": \"Resource-Type-#{uniqueResourceMark}-Updated\"," +
                            "\"description\": \"Resource-Description-Updated-#{uniqueResourceMark}\"}"))
                    .check(status().is(200))
                    .check(jsonPath("$.key").isEL("Updated-Resource-Key-#{uniqueResourceMark}"))
                    .check(jsonPath("$.type").isEL("Resource-Type-#{uniqueResourceMark}-Updated"))
                    .check(jsonPath("$.description").isEL("Resource-Description-Updated-#{uniqueResourceMark}")))
            .pause(pauseTime)

            // 2008. DELETE - Remove Resource
            .exec(http("2008. DELETE - Remove Resource")
                    .delete("/resource/#{myResourceId}")
                    .check(status().is(204)));


    /***   CLIENT RESOURCE ACCESS
     *     User session for Http Actions (POST PUT PATCH GET DELETE) is isolated   ***/

    private final Iterator<Map<String, Object>> accessFeeder =
            Stream.generate(() -> Collections.singletonMap("uniqueAccessMark", (Object) UUID.randomUUID().toString().substring(0, 8)))
                    .iterator();

    private final ChainBuilder accessCrudLifecycle = exec(
            // Feed a unique token string to this specific isolated user session
            feed(accessFeeder),

            // 3001. POST - Create its own dedicated record
            http("3001. POST - Create Client")
                    .post("/client")
                    .body(StringBody("{\"key\": \"CRA-Client-Key-#{uniqueAccessMark}\"}"))
                    .check(status().is(201))
                    // CAPTURE: Saves the database generated primary key into this user's PRIVATE session
                    .check(jsonPath("$.id").saveAs("myCRAClientId"))
                    .check(jsonPath("$.key").isEL("CRA-Client-Key-#{uniqueAccessMark}")),

            http("3001. POST - Create Resource")
                    .post("/resource")
                    .body(StringBody("{\"key\": \"CRA-Resource-Key-#{uniqueAccessMark}\"," +
                            "\"type\": \"CRA-Resource-Type-#{uniqueAccessMark}\"}"))
                    .check(status().is(201))
                    .check(jsonPath("$.id").saveAs("myCRAResourceId"))
                    .check(jsonPath("$.key").isEL("CRA-Resource-Key-#{uniqueAccessMark}"))
                    .check(jsonPath("$.type").isEL("CRA-Resource-Type-#{uniqueAccessMark}")),

            http("3001. POST - Create Client Resource Access")
                    .post("/client-resource-access")
                    .body(StringBody("{\"clientId\": \"#{myCRAClientId}\"," +
                            "\"resourceId\": \"#{myCRAResourceId}\"," +
                            "\"accessCode\": \"CRA-AccessCode-#{uniqueAccessMark}\"}"))
                    .check(status().is(201))
                    // CAPTURE: Saves the database generated primary key into this user's PRIVATE session
                    .check(jsonPath("$.clientId").isEL("#{myCRAClientId}"))
                    .check(jsonPath("$.resourceId").isEL("#{myCRAResourceId}"))
                    .check(jsonPath("$.accessCode").isEL("CRA-AccessCode-#{uniqueAccessMark}")),

            pause(pauseTime),

            // 3002. GET - Fetch only its own record
            http("3002. GET - Fetch Client Resource Access")
                    .get("/client-resource-access/client/#{myCRAClientId}/resource/#{myCRAResourceId}")
                    .check(status().is(200))
                    .check(jsonPath("$.clientId").isEL("#{myCRAClientId}"))
                    .check(jsonPath("$.resourceId").isEL("#{myCRAResourceId}"))
                    .check(jsonPath("$.accessCode").isEL("CRA-AccessCode-#{uniqueAccessMark}")),

            pause(pauseTime),

            // 3003. GET - Fetch the Key
            http("3003. GET - Fetch Client Resource Access - AccessCode")
                    .get("/client-resource-access/client/#{myCRAClientId}/resource/#{myCRAResourceId}/access-code")
                    .check(status().is(200))
                    .check(bodyString().isEL("CRA-AccessCode-#{uniqueAccessMark}")),

            pause(pauseTime),

            // 3005. PUT - Update only its own record
            http("3004. PUT - Replace Client Resource Access")
                    .put("/client-resource-access/client/#{myCRAClientId}/resource/#{myCRAResourceId}")
                    .body(StringBody("{\"accessCode\": \"Replaced-CRA-AccessCode-#{uniqueAccessMark}\"}"))
                    .check(status().is(200))
                    .check(jsonPath("$.clientId").isEL("#{myCRAClientId}"))
                    .check(jsonPath("$.resourceId").isEL("#{myCRAResourceId}"))
                    .check(jsonPath("$.accessCode").isEL("Replaced-CRA-AccessCode-#{uniqueAccessMark}")),

            pause(pauseTime),

            // 3006. PATCH - Patch only its own record
            http("3005. PATCH - Update Client Resource Access")
                    .patch("/client-resource-access/client/#{myCRAClientId}/resource/#{myCRAResourceId}")
                    .body(StringBody("{\"accessCode\": \"Updated-CRA-AccessCode-#{uniqueAccessMark}\"}"))
                    .check(status().is(200))
                    .check(jsonPath("$.clientId").isEL("#{myCRAClientId}"))
                    .check(jsonPath("$.resourceId").isEL("#{myCRAResourceId}"))
                    .check(jsonPath("$.accessCode").isEL("Updated-CRA-AccessCode-#{uniqueAccessMark}")),

            pause(pauseTime),

            // 3007. DELETE - Destroy its own record when completely finished
            http("3006. DELETE - Remove Client Resource Access")
                    .delete("/client-resource-access/client/#{myCRAClientId}/resource/#{myCRAResourceId}")
                    // Match against your controller's exact deletion response code (200 or 204)
                    .check(status().in(200, 204))

    );

    // Scenario structure forcing absolute isolation rules
    private final ScenarioBuilder accessCrudScenario = scenario("Isolated User CRUD Cycles - Client Resource Access")
            // Each user executes the complete sequence exactly 1 time from top to bottom
            .repeat(1).on(accessCrudLifecycle);



    // Set up
    {
        setUp(
                // Fire all 100 isolated users concurrently
                clientCrudScenario.injectOpen(atOnceUsers(loops)),

                // Fire 100 parallel users in 3 seconds
                resourceCrudScenario.injectOpen(nothingFor(Duration.ofSeconds(5)), rampUsers(loops).during(Duration.ofSeconds(30))),

                // Fire all 100 isolated users concurrently
                accessCrudScenario.injectOpen(nothingFor(Duration.ofSeconds(5)), atOnceUsers(loops))

        )
        .protocols(httpProtocol)
        .assertions(
                global().successfulRequests().percent().gte(90.0),
                global().responseTime().mean().lte(300),
                global().responseTime().percentile3().lte(600),
                global().failedRequests().count().lte(5L)
        );
    }

}
