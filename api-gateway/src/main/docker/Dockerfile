FROM openjdk:9
VOLUME /tmp
EXPOSE 9004 9005
ADD stela-api-gateway.jar app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Xmx1024m","-jar","/app.jar"]
