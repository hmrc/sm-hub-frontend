#!/usr/bin/env bash

echo "Starting sm-hub on port 1024"
~/Applications/sm-hub-frontend/sm-hub-frontend-VERSION/sm-hub-frontend-VERSION/bin/sm-hub-frontend -DsmPath=$WORKSPACE/service-manager-config -DgithubOrg=hmrc -Dworkspace=$WORKSPACE -Dhttp.port=1024 -DcurrentVersion=VERSION > ~/Applications/sm-hub-frontend/logs 2>&1 &

echo "sm-hub can be found at http://localhost:1024/sm-hub/running-services"