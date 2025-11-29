FROM ubuntu:22.04
LABEL authors="tanaka"

# Install Java runtime (minimal OpenJDK)
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-21-jre-headless \
    && rm -rf /var/lib/apt/lists/*

# Copy your Java application
COPY app.jar /app/app.jar

WORKDIR /app

# Expose port (change 8080 to your application's port)
EXPOSE 8080

# Run your Java application
ENTRYPOINT ["java", "-jar", "app.jar"]