FROM openjdk:8
VOLUME /tmp
EXPOSE 9004 9005
ADD api-gateway.jar app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
