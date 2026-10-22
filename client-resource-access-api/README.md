# Client-Resource-Access-API

This standalone microservice features an independent backend database with dedicated **[DDL](https://github.com/gsk-2026/java-api/blob/main/client-resource-access-api/data/client_resource_access_api.sql)**.
The scope of the Client-Resource-Access-API encompasses core development, quality assurance (AQ) testing, and automated continuous integration/continuous deployment (CI/CD) pipelines.

---


## Structural Overview & Scalability Architecture

```text
java-api/ (Standalone Workspace Root)
│
├── src/                               # Core source directory for java-api code base
├── .gitignore                         # Local repository file tracking exclusions
├── pom.xml                            # Independent Maven configuration for java-api
├── Jenkinsfile                        # CI/CD orchestration pipeline definition 
├── README.md                          # Main workspace documentation (This File)
│
├── client-resource-access-api/        # [Active Microservice Project Module]
│   ├── .mvn/                          # Maven wrapper binaries and settings
│   ├── src/                           
│   │   ├── main/                      # Application layer
│   │   │   ├── java/                  # Java controller, service, domain packages
│   │   │   └── resources/             # application.properties, banners, YAMLs
│   │   └── test/                      
│   │       ├── java/                  # Unit, Integration, and Load tests
│   │       └── resources/             # Test properties, SQL seeds, Gatling assets
│   ├── .gitignore                     # Service-specific file exclusion rules
│   ├── Dockerfile                     # OCI Container construction instructions 
│   ├── mvnw / mvnw.cmd                # Cross-platform Maven wrapper binaries
│   ├── pom.xml                        # Inherits directly from spring-boot-starter-parent
│   │
│   └── Local Automation Scripts:
│       ├── build_clean_compile.bat    # Windows: Compiles source and processors
│       ├── build_clean_compile.sh     # Unix/Bash: Compiles source and processors
│       ├── build_clean_package.bat    # Windows: Packages code into an executable .jar
│       ├── build_clean_package.sh     # Unix/Bash: Packages code into an executable .jar
│       ├── build_clean_test.bat       # Windows: Triggers local unit tests
│       ├── build_clean_test.sh        # Unix/Bash: Triggers local unit tests
│       ├── build_clean_verify.bat     # Windows: Runs full test and verification loops
│       └── build_clean_verify.sh      # Unix/Bash: Runs full test and verification loops
```

---


## Required Installation Verification

Below is the list of minimum installation requirements to run tests for the `client-resource-access-api`:

### 1. Java

```bash
java --version                           # sample return: java 26 
```

### 2. mvn

```bash
mvn --version                            # sample return: Apache Maven 3.9.16 
```

### 3. docker

```bash
docker --version                         # required to run ClientResourceAccessApiDockerE2E, ClientResourceAccessApiE2ELoadRunner, and Jenkins CI/CD pipeline
```


### 4. Database (e.g: Oracle DB)

```bash
lsnrctl status                           # The DB could also be a docker container, not a native service.  Required to run ClientResourceAccessApiOracleE2E
                                         # Please not ro set 'ddl-auto: create' for the first run, which will automatically create db schema. then reset back to 'ddl-auto: update'
```

### 5. Jenkins

```bash
sc query Jenkins                         # Jenkins could also be a docker container, or a browser application.  Required for CI/CD pipeline
```

---


## Steps to Manually Execute Quality Testing Suites

The examples below show how to manually run the full quality testing workflow:

### 1. Clone the Codebase
```bash
git clone https://github.com/gsk-2026/java-api java-api-local
cd java-api-local/client-resource-access-api
```

### 2. Run all/specific config unit testing
```bash
# Windows (Git Bash/ CMD / PowerShell)
mvn clean test -Dtest="com.dreamtech.clientresourceaccessapi.config.*Test"                      # all testing
mvn clean test -Dtest="com.dreamtech.clientresourceaccessapi.config.SecurityConfigTest"         # specific testing
```

### 3. Run all/specific controller unit testing
```bash
mvn clean test -Dtest="com.dreamtech.clientresourceaccessapi.controller.*Test"                  # all testing
mvn clean test -Dtest="com.dreamtech.clientresourceaccessapi.controller.ClientControllerTest"   # specific testing
```

### 4. Run all/specific repository unit testing
```bash
mvn clean test -Dtest="com.dreamtech.clientresourceaccessapi.repository.*Test"                  # all testing
mvn clean test -Dtest="com.dreamtech.clientresourceaccessapi.repository.ClientRepositoryTest"   # specific testing
```

### 5. Run all/specific service unit testing
```bash
mvn clean test -Dtest="com.dreamtech.clientresourceaccessapi.service.*Test"                     # all testing
mvn clean test -Dtest="com.dreamtech.clientresourceaccessapi.service.ClientServiceImplTest"     # specific testing
```

### 6. Run all (controller, repository, service) unit testing
```bash
mvn clean test 
```

### 7. Run all/specific integration testing (IT)
```bash
mvn clean test -Dtest="com.dreamtech.clientresourceaccessapi.*IT"                              # all testing
mvn clean test -Dtest="com.dreamtech.clientresourceaccessapi.ClientResourceAccessIT"           # specific testing
```

### 8. Run all/specific End-to-End (E2E) testing
```bash
mvn clean test -Dtest="com.dreamtech.clientresourceaccessapi.*E2E"                              # all testing
mvn clean test -Dtest="com.dreamtech.clientresourceaccessapi.ClientResourceAccessApiDockerE2E"  # specific testing
```

### 9. Run performance testing
```bash
mvn clean package        # 1. Backage the compiled code into a JAR file
java -Dspring.profiles.active=qatest -jar target/client-resource-access-api-0.0.1-SNAPSHOT.jar    # 2. launches the API package
mvn gatling:test -Dgatling.simulationClass=com.dreamtech.clientresourceaccessapi.ClientResourceAccessApiE2ELoadScenario      # 3. trigger the peformance tetsing
### The performance tets result will be presented in a file - refer the at the end of the logs
```

### 10. Run all (unit, integration, end-to-end, performance) testing
```bash
mvn clean verify -Pqatest
```

### 11. Launch the API package
```bash
mvn clean package       # 1. Package the compiled code into a JAR file
java -Dspring.profiles.active=qatest -jar target/client-resource-access-api-0.0.1-SNAPSHOT.jar    # 2. launches the API package
http://localhost:8181/swagger-ui/index.html         # 3. Access the Swagger UI by navigating to this URL in your web browser
```

---


##  CI/CD Automation Engine

1. Integrate Jenkins with GitHub
2. The root project incorporates a multi-tier Jenkinsfile configuration script
3. The CI/CD pipeline executes all unit, integration, E2E, and performance tests.
4. Every branch commit pushed to repository features will initiate the unit tests only.
5. Every branch commit pushed to repository main will trigger integration, end-to-end, and performance tests.
6. Jenkins pipeline can also be triggered manually from Jenkins (Build with Parameters)

---