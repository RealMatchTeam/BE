FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 6000

ENV SERVER_PORT=6000

ENTRYPOINT ["java", "-jar", "app.jar"]