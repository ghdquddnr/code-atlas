FROM node:22-alpine AS frontend-build
WORKDIR /frontend

COPY frontend/package*.json ./
RUN npm ci
COPY frontend ./
RUN npm run build

FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle build.gradle ./
COPY src src
COPY --from=frontend-build /frontend/dist src/main/resources/static

RUN chmod +x ./gradlew
RUN ./gradlew bootJar --no-daemon -PskipFrontendBuild=true

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
