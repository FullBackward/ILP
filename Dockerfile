#FROM --platform=linux/amd64 openjdk:17
FROM openjdk:17
LABEL authors="winstonren"

EXPOSE 8080

WORKDIR /app

COPY ./target/PizzaDronz-1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]