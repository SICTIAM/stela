FROM openjdk:8
VOLUME /tmp
EXPOSE 9411
ADD stela-tracing-service.jar app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
