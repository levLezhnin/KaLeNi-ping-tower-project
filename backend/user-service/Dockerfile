# Билд стадия
FROM gradle:8.4-jdk17 AS build
WORKDIR /app
COPY build.gradle ./
COPY settings.gradle ./
COPY src ./src
RUN gradle build -x test --no-daemon
# Финальная стадия
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar user-service-app.jar
EXPOSE ${USER_SERVICE_PORT}
ENTRYPOINT ["java", "-jar", "user-service-app.jar"]