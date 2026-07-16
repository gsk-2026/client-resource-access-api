package com.dreamtech.clientresourceaccessapi;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.oracle.OracleContainer;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.time.Duration;
import java.util.Properties;


public class ClientResourceAccessApiE2ELoadRunner {

    static void main(String[] args) {

        // 1. Configure Oracle with performance overrides & strict resource quotas
        OracleContainer oracleContainer = new OracleContainer("gvenzl/oracle-free:23-slim-faststart")
                .withUsername("e2e_username")
                .withPassword("secure_e2e_password")
                // Mitigate connection pool thrashing during high-concurrency loads
                .withReuse(false)
                // Ensure Oracle internal processes are entirely ready before unblocking Spring
                .waitingFor(Wait.forLogMessage(".*DATABASE IS READY TO USE!.*\\s*", 1)
                        .withStartupTimeout(Duration.ofMinutes(5)));

        // Start Oracle Docker container
        oracleContainer.start();

        // The connection detail
        System.out.println("Docker Container Datasource JDBC URL: " + oracleContainer.getJdbcUrl());

        // 2. Guarantee container cleanup on exit even if the load runner JVM terminates abruptly
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Stopping Docker Container for performance isolation infrastructure...");
            oracleContainer.stop();
        }));

        // 3 Construct custom configuration strings into an array that behaves like command line inputs
        String serverPort = getPortFromProperties();

        String[] customizedArgs = new String[] {
                "--spring.profiles.active=load-test",
                "--spring.jpa.hibernate.ddl-auto=update",
                "--spring.datasource.url=" + oracleContainer.getJdbcUrl(),
                "--spring.datasource.username=" + oracleContainer.getUsername(),
                "--spring.datasource.password=" + oracleContainer.getPassword(),
                "--server.port=" + serverPort,
                "--spring.datasource.hikari.maximum-pool-size=50",
                "--logging.level.root=WARN"
        };

        // Combine original args with your container overrides
        String[] customArgs = java.util.stream.Stream.concat(
                java.util.Arrays.stream(args),
                java.util.Arrays.stream(customizedArgs)
        ).toArray(String[]::new);

        // 4. Boot up the Spring application context holding these values alive
        ConfigurableApplicationContext context = new SpringApplicationBuilder(ClientResourceAccessApiApplication.class)
               .run(customArgs);

        // Extract the actual DataSource Bean that Spring created using your map
        DataSource dataSource = context.getBean(DataSource.class);
        printActiveDatasource(dataSource);

        System.out.println("🚀 Spring Boot application context is running at: http://localhost:" + serverPort + "/api/v1");

        // 5. Trigger the Gatling directly from cmd line:
        //    mvn gatling:test -Dgatling.simulationClass=com.dreamtech.clientresourceaccessapi.ClientResourceAccessApiE2ELoadScenario
        System.out.println("🔥 Launching Gatling Load Simulation...");

        int gatlingExitCode = -1;
        try {
            // Determine if we are executing inside a Windows operating system environment
            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

            String[] mavenCommand;
            if (isWindows) {
                // Use the project's local Maven wrapper script for Windows execution environments
                mavenCommand = new String[] {
                        "cmd.exe", "/c", "mvnw.cmd", "gatling:test",
                        "-Dgatling.simulationClass=" + ClientResourceAccessApiE2ELoadScenario.class.getName()
                };
            } else {
                // Standard Unix/Linux/macOS layout script execution
                mavenCommand = new String[] {
                        "./mvnw", "gatling:test",
                        "-Dgatling.simulationClass=" + ClientResourceAccessApiE2ELoadScenario.class.getName()
                };
            }

            // Set up the process builder to stream real-time logs to your IDE window
            ProcessBuilder processBuilder = new ProcessBuilder(mavenCommand);
            processBuilder.redirectErrorStream(true);
            processBuilder.directory(new java.io.File(System.getProperty("user.dir")));
            Process process = processBuilder.start();

            // Stream Gatling console results in real-time
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[GATLING] " + line);
                }
            }

            // Wait for the simulation run to finish
            gatlingExitCode = process.waitFor();
            System.out.println("🏁 Gatling test finished execution with code: " + gatlingExitCode);

        } catch (Exception e) {
            System.err.println("❌ Critical failure launching Gatling simulation process: " + e);
        }

        // 6. Gracefully shut down Spring and let the hook stop Docker
        context.close();
        System.exit(gatlingExitCode);

    }

    // A. Clean diagnostic print helper
    private static void printActiveDatasource(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("Spring Application Datasource JDBC URL: " + connection.getMetaData().getURL());
        } catch (Exception e) {
            System.err.println("Failed to read active datasource connection info: " + e);
        }
    }

    private static String getPortFromProperties() {
        Properties prop = new Properties();

        try (InputStream input = ClientResourceAccessApiE2ELoadRunner.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                return  "8080"; // Default fallback if file is missing
            }
            prop.load(input);
            return prop.getProperty("server.port", "8080");

        } catch (Exception ex) {
            return "8080"; // Default fallback on error
        }
    }
}



