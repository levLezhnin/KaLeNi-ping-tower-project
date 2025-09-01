# Билд стадия
FROM gradle:8.4-jdk17 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle build -x test --no-daemon

# Финальная стадия
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/Ping-Tower-Backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]