FROM openjdk:8-jre-alpine
VOLUME /tmp
EXPOSE 9010 9011
ADD stela-pes-service.jar app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS=""
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Xmx4096m", "-jar", "/app.jar"]
