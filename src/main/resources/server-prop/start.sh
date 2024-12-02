#!/bin/bash
set -x
VAR=""
#DPATH="/u01/eirapp/APIService4/3.1.2/"
PNAME="webparser_internal.jar"
#cd $DPATH
status=`ps -ef | grep $PNAME | grep java`
if [ "$status" != "$VAR" ]
then
 echo "The process is already running"
 echo $status
else
 echo "Starting process"
 java -Dlog4j.configurationFile=file:./log4j2.xml -Dmodule.name=webparser_internal -Dspring.config.location=file:./application.properties,file:${APP_HOME}/configuration/configuration.properties -jar $PNAME 1>/dev/null &
 echo "Process started"
fi

