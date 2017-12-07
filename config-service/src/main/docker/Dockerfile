FROM openjdk:8
VOLUME /tmp
EXPOSE 8888
RUN mkdir -p /root/.ssh
RUN chmod 700 /root/.ssh
RUN ssh-keyscan -t rsa -p 6666 forge.sictiam.fr > /root/.ssh/known_hosts
#id_rsa is copied from ~/.shh folder at build time
ADD id_rsa /root/.ssh/id_rsa
ADD stela-config-service.jar app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
