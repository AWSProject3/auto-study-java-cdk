# Stage 1: Build Java
FROM gradle:8.5-jdk17 as java-build

WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

RUN chmod +x gradlew

RUN ./gradlew --no-daemon dependencies

COPY src ./src
RUN ./gradlew --no-daemon build

# Stage 2: Build TypeScript
FROM node:18 as ts-build

WORKDIR /app/eks

COPY eks/package*.json ./
RUN npm ci

COPY eks .
RUN npm run build

# Stage 3: Run
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=java-build /app/build/libs/*.jar app.jar
COPY --from=ts-build /app/eks/lib ./eks/lib

ENTRYPOINT ["java", "-jar", "app.jar"]

EXPOSE 8080
