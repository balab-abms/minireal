#!/bin/bash
# Copy files from temporary location to target directory
cp -r /tmp/minireal_data/* /usr/src/app/minireal_data/
# Start the application
java -jar /usr/src/app/app.jar
