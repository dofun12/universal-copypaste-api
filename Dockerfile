FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring

COPY --from=build /workspace/target/*.jar /app/app.jar

RUN mkdir -p /app/data && chown -R spring:spring /app
USER spring

EXPOSE 8080
VOLUME ["/app/data"]

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
