#!/bin/bash
# Copy files from temporary location to target directory
cp -r /tmp/simreal_data/* /usr/src/app/simreal_data/
# Start the application
java -jar /usr/src/app/app.jar
