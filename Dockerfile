FROM eclipse-temurin:25-jre
RUN useradd --system --create-home app
WORKDIR /app
COPY target/app.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
