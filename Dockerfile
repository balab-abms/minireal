FROM eclipse-temurin:21-jre
WORKDIR /usr/src/app
COPY target/*.jar /usr/src/app/app.jar
COPY minireal_data/ /usr/src/app/minireal_data/
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/usr/src/app/app.jar"]
