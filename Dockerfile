# Build stage
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copy Maven wrapper and project files
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

# Copy module projects
COPY vantage-common vantage-common
COPY vantage-framework vantage-framework
COPY vantage-modules vantage-modules
COPY vantage-admin vantage-admin

# Build the application
RUN ./mvnw clean package -DskipTests -q

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Create non-root user for security
RUN groupadd -r vantage && useradd -r -g vantage vantage

# Copy the built artifact
COPY --from=build /app/vantage-admin/target/*.jar app.jar

# Create data directory
RUN mkdir -p /app/data && chown -R vantage:vantage /app

# Switch to non-root user
USER vantage

# Expose port
EXPOSE 8081

# Volume for H2 database persistence
VOLUME ["/app/data"]

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]