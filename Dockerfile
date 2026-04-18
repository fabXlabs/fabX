# syntax=docker/dockerfile:1

FROM gcr.io/distroless/java25-debian13
WORKDIR /app
COPY app/build/libs/app-*-all.jar ./app.jar
CMD ["app.jar"]
