FROM openjdk:8-jre-alpine
VOLUME /tmp
EXPOSE 8888
ADD stela-config-service.jar app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Xmx1024m","-jar","/app.jar"]
