FROM openjdk:8
VOLUME /tmp
EXPOSE 9000 9001
ADD stela-pes-service.jar app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]