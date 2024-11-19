FROM eclipse-temurin:21-jre
WORKDIR /usr/src/app
COPY target/*.jar /usr/src/app/app.jar
COPY minireal_data /tmp/minireal_data
COPY startup.sh /usr/src/app/startup.sh
RUN chmod +x /usr/src/app/startup.sh
EXPOSE 8090
ENTRYPOINT ["/bin/bash", "/usr/src/app/startup.sh"]
