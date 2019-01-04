FROM openjdk:8-jre-alpine
VOLUME /tmp
EXPOSE 9008 9009
ADD stela-acte-service.jar app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Xmx4096m -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]