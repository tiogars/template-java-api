FROM eclipse-temurin:25-jre
RUN useradd --system --create-home app
WORKDIR /app
COPY target/app.jar app.jar
USER app
EXPOSE 8080
# The JRE image ships without curl/wget: probe /actuator/health with bash's /dev/tcp.
HEALTHCHECK --interval=15s --timeout=3s --start-period=60s --retries=5 \
    CMD ["bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/8080 && printf 'GET /actuator/health HTTP/1.0\\r\\nHost: localhost\\r\\n\\r\\n' >&3 && grep -q '\"UP\"' <&3"]
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
