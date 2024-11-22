FROM eclipse-temurin:21-jre
# Set the working directory
WORKDIR /usr/src/app
# Copy the application JAR file
COPY target/*.jar /usr/src/app/app.jar
# Copy the data directory
COPY minireal_data/ /usr/src/app/minireal_data/
# Install necessary tools and Gradle
RUN apt-get update && apt-get install -y unzip
RUN unzip -d /opt/gradle /usr/src/app/minireal_data/gradle_data/gradle-8.4-bin.zip
RUN ln -s /opt/gradle/gradle-8.4/bin/gradle /usr/bin/gradle
# Create the profile.d directory if it doesn't exist and add the gradle.sh file
RUN mkdir -p /etc/profile.d && echo "export PATH=\$PATH:/usr/share/gradle/bin" > /etc/profile.d/gradle.sh
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/usr/src/app/app.jar"]
