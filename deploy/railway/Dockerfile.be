# Railway: Spring Boot API
# Copy of be-doc-doctor-ri/Dockerfile — set Railway Root Directory to be-doc-doctor-ri
# and Dockerfile path to Dockerfile (preferred), or point here if using deploy/ as context.

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
COPY --from=build /app/target/*-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
