#!/bin/bash
process=webParser.jar
script_name="./monitor.sh"
count=`ps -ef | grep $process | grep -v grep | grep -v "$script_name" | wc -l`
if [ $count -gt 1 ]
then
        echo "Multiple Instances Running"
        echo
        ps -ef | grep $process | grep -v grep | grep -v "$script_name"
elif [ $count -eq 0 ]
then
        echo "No Process Running"
elif [ $count -eq 1 ]
then
        echo "Process Running"
        echo
        ps -ef | grep $process | grep -v grep | grep -v "$script_name"
else
        echo "No Process Found"
fi
