FROM openjdk:8
VOLUME /tmp
EXPOSE 7979
ADD hystrix-dashboard.jar app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]