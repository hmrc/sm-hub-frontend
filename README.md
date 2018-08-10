[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

sm-hub-frontend
================================

How to run: Everyday use
========================
```bash
curl --silent https://raw.githubusercontent.com/hmrc/sm-hub-frontend/implicit-startup/sm-hub-install.sh | bash
```

This will download the latest version of sm-hub-frontend and place start and stop command in `/usr/local/bin`

Once installed you can use 

```bash
sm-hub-start
```

To start sm-hub on port `1024`

and 

```bash
sm-hub-stop
```

To kill the current sm-hub process

To update sm-hub simply re run the install command as it will kill the current sm-hub process, purge your current version and download the latest.


How to run: Local development
=============================

```sbtshell
sbt -DsmPath=/Users/foo/bar/config-location -Dworkspace=/Users/foo/bar/location-of-services -DgithubOrg=your-github-org-name run
```

This will start the application on port **1024**

You also need to provide the path to your service manager config and workspace using a -D args (replace smPath, workspace and githubOrg in the above example)

How to test
===========
```sbtshell
sbt clean coverage test coverageReport
```

Features
========
- See currently running services based your teams sm profile
- Search through all available profiles
- Search through all available services
- See which ports are available to use
- See conflicting port usages
- Catalogue of test routes for services
- See currently available versions of assets frontend
- Generate config

License
=======

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

