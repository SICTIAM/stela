FROM openjdk:9
VOLUME /tmp
EXPOSE 9012 9013
ADD stela-convocation-service.jar app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Xmx1024m -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]