FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/vehicle-domain-service-0.1.0.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]