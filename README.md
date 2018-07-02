[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

sm-hub-frontend
================================

How to run
==========

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

