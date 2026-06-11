# Stage 1: Build React frontend
FROM node:20-alpine AS frontend
WORKDIR /app
COPY vantage-ui/package*.json vantage-ui/
RUN cd vantage-ui && npm ci
COPY vantage-ui/ vantage-ui/
RUN cd vantage-ui && npm run build

# Stage 2: Build Spring Boot backend
FROM eclipse-temurin:17-jdk AS backend
WORKDIR /app
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn
COPY vantage-admin/ vantage-admin/
COPY --from=frontend /app/vantage-ui/dist/ vantage-admin/src/main/resources/static/
RUN chmod +x mvnw && ./mvnw clean package -DskipTests -q

# Stage 3: Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd -r vantage && useradd -r -g vantage vantage
COPY --from=backend /app/vantage-admin/target/*.jar app.jar
RUN mkdir -p /app/data && chown -R vantage:vantage /app
USER vantage
EXPOSE 8080
VOLUME ["/app/data"]
ENTRYPOINT ["java", "-jar", "app.jar"]
