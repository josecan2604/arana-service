FROM openjdk:17-jdk-slim
COPY  target/file-api-0.0.1-SNAPSHOT-LOCAL.jar /app.jar
ENTRYPOINT ["java", "-Xmx50m", "-Xms50m", "-jar", "/app.jar"]