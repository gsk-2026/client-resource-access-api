# Java-API

This is a standalone, top-level Maven repository designed to house multiple independent microservices side-by-side. It serves as a unified workspace that simplifies project organization while maintaining strict isolation between these microservices

---


## Structural Overview & Scalability Architecture

The `java-api` directory operates as an architectural folder hub. New microservices could be added as standalone folders sitting parallel to each other. None of these sub-projects share runtime contexts or parent pom inheritances, ensuring a completely decoupled microservice architecture.

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
│
├── new-a-api/                         # [Future New A Standalone Microservice Folder Example]
│
├── new-b-api/                         # [Future New B Standalone Microservice Folder Example]
```

---


## Required Installation Verification

The following section provides general installation and verification instructions.  For detailed step-by-step procedures, please refer to the README.md file within each individual module.

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


## Environmental Verification Matrix

| Target Microservice            | API Base URLs                           | Framework Engine | Suite Status |
|:-------------------------------|:----------------------------------------|:-----------------|:-------------|
| `client_resource_access_api`   | github/.../client-resource-access-api   | Pytest & Locust  | 🟢 Active    |
| `practice_softwaretesting_api` | https://v4.practicesoftwaretesting.com  | Pytest & Locust  | 🟢 Active    | 
| `a_api` / `b_api`              | Scaled Business Testing Sub-systems     | Pytest           | 🟡 Planning  |


---


##  CI/CD Automation Engine

1. Integrate Jenkins with GitHub
2. The root project incorporates a multi-tier Jenkinsfile configuration script
3. The CI/CD pipeline executes all unit, integration, E2E, and performance tests.
4. Every branch commit pushed to repository features will initiate the unit tests only.
5. Every branch commit pushed to repository main will trigger integration, end-to-end, and performance tests.
6. Jenkins pipeline can also be triggered manually from Jenkins (Build with Parameters)

---