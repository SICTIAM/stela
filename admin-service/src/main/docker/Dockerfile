FROM openjdk:8
VOLUME /tmp
EXPOSE 9006 9007
ADD stela-admin-service.jar app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]