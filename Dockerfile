# Start with a base image containing Java runtime
FROM openjdk:17

# Define a default value for the JAR file
ARG JAR_FILE=target/bogBot-0.3.0-SNAPSHOT.jar

# Create a directory for dependencies
RUN mkdir -p /app-lib

# Add the application's JAR to the container
ADD ${JAR_FILE} /app.jar

# Add SLF4J and its dependencies to the container
COPY target/dependency/*.jar /app-lib/

# Add the token.yaml file to the container
COPY target/classes/token.yaml /app/resources/
COPY target/classes/dbConfig.yaml /app/resources/
COPY target/classes/timerConfig.yaml /app/resources/

# Run the JAR file with the correct classpath
ENTRYPOINT ["java", "-cp", "/app.jar:/app-lib/*", "org.bog.bot.bogBotApp.BogBotMain"]