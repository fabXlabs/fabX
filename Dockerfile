# syntax=docker/dockerfile:1

FROM azul/zulu-openjdk-alpine:16-jre
WORKDIR /app
COPY app/build/libs/app-*-all.jar ./app.jar

CMD ["java", "-jar", "app.jar"]