FROM openjdk:8
VOLUME /tmp
EXPOSE 9002 9003
ADD stela-pes-dgfip-sender.jar app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
