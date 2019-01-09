#!/usr/bin/env bash

echo "Killing existing sm-hub processes and cleaning current sm-hub"
$pid
pid=$(lsof -i -a | grep 1024 | awk -F" " '{print $2}')
kill -9 $pid
rm ~/Applications/sm-hub-frontend/sm-hub-frontend-VERSION/sm-hub-frontend-VERSION/RUNNING_PID

rm -rf ~/Applications/sm-hub-frontend

sudo rm /usr/local/bin/sm-hub-start
sudo rm /usr/local/bin/sm-hub-stop

echo "Creating new home for sm-hub"
mkdir -p ~/Applications/sm-hub-frontend/

$latest_version

echo "Getting latest version of sm-hub"
#latest_version="$(curl --silent "https://api.github.com/repos/hmrc/sm-hub-frontend/releases/latest" | grep '"name":' | sed -E 's/.*"([^"]+)".*/\1/')"
#Intermediate fix to get install script working
latest_version="0.10.0"
echo "Downloading version $latest_version of sm-hub-frontend to $HOME/Applications/sm-hub-frontend"
curl -L https://hmrc.bintray.com/releases/uk/gov/hmrc/sm-hub-frontend_2.11/$latest_version/sm-hub-frontend_2.11-$latest_version.tgz --output ~/Applications/sm-hub-frontend/sm-hub-frontend-$latest_version.tgz

mkdir -p ~/Applications/sm-hub-frontend/sm-hub-frontend-$latest_version

echo "Unpacking sm-hub from tgz"
tar -xzf ~/Applications/sm-hub-frontend/sm-hub-frontend-$latest_version.tgz --directory ~/Applications/sm-hub-frontend/sm-hub-frontend-$latest_version

rm ~/Applications/sm-hub-frontend/sm-hub-frontend-$latest_version.tgz

echo "Downloaded version $latest_version of sm-hub-frontend to $HOME/Applications/sm-hub-frontend"

echo "Fetching sm-hub start and stop scripts"
sudo curl --silent -L https://raw.githubusercontent.com/hmrc/sm-hub-frontend/master/sm-hub-start.sh --output /usr/local/bin/sm-hub-start
sudo curl --silent -L https://raw.githubusercontent.com/hmrc/sm-hub-frontend/master/sm-hub-stop.sh --output /usr/local/bin/sm-hub-stop

sudo chmod +x /usr/local/bin/sm-hub-*

sudo sed -i -e "s/VERSION/${latest_version}/g" /usr/local/bin/sm-hub-start
sudo sed -i -e "s/VERSION/${latest_version}/g" /usr/local/bin/sm-hub-stop

sudo rm /usr/local/bin/sm-hub-*-e

sudo chown `whoami`:admin /usr/local/bin/sm-hub-start
sudo chown `whoami`:admin /usr/local/bin/sm-hub-stop
sudo chown -R `whoami`:staff ~/Applications/sm-hub-frontend

echo "Installation complete"
echo "To update sm-hub re run this command"
echo "To start sm-hub run 'sm-hub-start' from any terminal"
echo "To stop sm-hub run 'sm-hub-stop' from any terminal"
echo "Logs can be found in $HOME/Applications/sm-hub-frontend/logs"


