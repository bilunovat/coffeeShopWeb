# Stage 1: Install dependencies #################################################
FROM maven:3.9-eclipse-temurin-17 AS deps

WORKDIR /build

COPY pom.xml .

RUN mvn dependency:go-offline -DskipTests

# Stage 2: Build the Java app ###################################################
FROM deps AS package

WORKDIR /build

COPY ./ .
RUN mvn package -DskipTests && \
    mv target/*.jar target/app.jar

# Stage 3: Run the application ##################################################
FROM eclipse-temurin:17-jre-jammy AS final

ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser
USER appuser

COPY --from=package /build/target/app.jar app.jar

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "app.jar" ]
