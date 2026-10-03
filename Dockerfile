FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml common-library/pom.xml
COPY src common-library/src
RUN mvn --batch-mode -f common-library/pom.xml -DskipTests package \
    && cp common-library/target/common-library-1.0.0.jar /app.jar

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build --chown=10001:10001 /app.jar app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
