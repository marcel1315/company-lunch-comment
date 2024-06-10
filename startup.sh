#!/bin/bash

JAR_NAME="company-lunch-comment-0.0.1-SNAPSHOT.jar"
LOG_FILE="/home/ec2-user/deploy.log"
ERROR_LOG_FILE="/home/ec2-user/deploy_err.log"
DEPLOY_PATH="/home/ec2-user"
BUILD_PATH="/home/ec2-user/build"
JAVA_OPTS=""

echo "> build filename: $JAR_NAME" >> $LOG_FILE

echo "> build file copy" >> $LOG_FILE
cp $BUILD_PATH/*.jar $DEPLOY_PATH

echo "> Application PID if there is one running" >> $LOG_FILE
CURRENT_PID=$(pgrep -f $JAR_NAME)

if [ -z "$CURRENT_PID" ]; then
    echo "> No application running" >> $LOG_FILE
else
    echo "> kill -9 $CURRENT_PID" >> $LOG_FILE
    kill -9 $CURRENT_PID
    JAVA_OPTS="-Dspring.jpa.hibernate.ddl-auto=none"
    sleep 5
fi

BUILD_JAR=$DEPLOY_PATH/$JAR_NAME
echo "> BUILD_JAR Deploy" >> $LOG_FILE
nohup java $JAVA_OPTS -jar $BUILD_JAR >> $LOG_FILE 2>> $ERROR_LOG_FILE &
