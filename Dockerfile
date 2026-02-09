FROM eclipse-temurin:8-jre-alpine

WORKDIR /usr/app

# copy the built jar (whatever name it has) into a fixed name
COPY build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
