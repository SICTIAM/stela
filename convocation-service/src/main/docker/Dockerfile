FROM openjdk:8
VOLUME /tmp
EXPOSE 9012 9013
ADD stela-convocation-service.jar app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS=""
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Xmx1024m","-jar","/app.jar"]