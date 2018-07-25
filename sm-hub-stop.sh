#!/usr/bin/env bash
echo "Killing sm-hub process"
$pid
pid=$(lsof -i -a | grep 1024 | awk -F" " '{print $2}')
kill -9 $pid
rm ~/Applications/sm-hub-frontend/sm-hub-frontend-VERSION/sm-hub-frontend-VERSION/RUNNING_PID