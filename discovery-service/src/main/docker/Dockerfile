FROM openjdk:8-jre-alpine
VOLUME /tmp
EXPOSE 8761
ADD stela-discovery-service.jar app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Xmx1024m","-jar","/app.jar"]