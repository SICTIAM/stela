FROM openjdk:8
VOLUME /tmp
EXPOSE 8888
ADD stela-config-service.jar app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Xmx128m","-jar","/app.jar"]
