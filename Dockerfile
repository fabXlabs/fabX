# syntax=docker/dockerfile:1

FROM azul/zulu-openjdk-alpine:16-jre
RUN mkdir /app
RUN addgroup --system appuser && adduser -S -s /bin/false -G appuser appuser
WORKDIR /app
COPY app/build/libs/app-*-all.jar ./app.jar
RUN chown -R appuser:appuser /app
USER appuser

CMD ["java", "-jar", "app.jar"]