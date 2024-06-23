#!/bin/bash

# Firebase config
export GOOGLE_APPLICATION_CREDENTIALS="/home/ec2-user/build/config/our-company-lunch-firebase-adminsdk.json"

# Run jar
JAR_NAME="company-lunch-comment-0.0.1-SNAPSHOT.jar"
LOG_FILE="/home/ec2-user/deploy.log"
ERROR_LOG_FILE="/home/ec2-user/deploy_err.log"
DEPLOY_PATH="/home/ec2-user"
BUILD_PATH="/home/ec2-user/build"
JAVA_OPTS=""

echo "> Build filename: $JAR_NAME" >> $LOG_FILE

echo "> Build file copy" >> $LOG_FILE
cp $BUILD_PATH/*.jar $DEPLOY_PATH

echo "> Get application PID if there is one running" >> $LOG_FILE
CURRENT_PID=$(pgrep -f $JAR_NAME)

if [ -z "$CURRENT_PID" ]; then
    echo "> No application running" >> $LOG_FILE
else
    echo "> kill -9 $CURRENT_PID" >> $LOG_FILE
    kill -9 $CURRENT_PID
    sleep 5
fi

JAVA_OPTS="-Dspring.jpa.hibernate.ddl-auto=update"

BUILD_JAR=$DEPLOY_PATH/$JAR_NAME
echo "> BUILD_JAR $BUILD_JAR" >> $LOG_FILE
echo "> JAVA_OPTS $JAVA_OPTS" >> $LOG_FILE
nohup java $JAVA_OPTS -jar $BUILD_JAR >> $LOG_FILE 2>> $ERROR_LOG_FILE &
