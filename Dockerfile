# Stage 1 — build
FROM gradle:jdk25 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar -x test --no-daemon

# Stage 2 — runtime
FROM eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /app
COPY --from=build /app/build/libs/JobBoardAPI-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV SPRING_DATASOURCE_URL=""
ENV SPRING_DATASOURCE_USERNAME=""
ENV SPRING_DATASOURCE_PASSWORD=""

ENTRYPOINT ["java", "-jar", "app.jar"]
