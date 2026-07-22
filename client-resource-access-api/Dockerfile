FROM eclipse-temurin:26-jre

WORKDIR /app

COPY target/client-resource-access-api-*.jar  app.jar

RUN useradd -m spring
USER spring

EXPOSE 8181

ENTRYPOINT ["java", "-jar", "app.jar"]