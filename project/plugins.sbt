/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

logLevel := Level.Warn

resolvers += Resolver.url("HMRC Sbt Plugin Releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
resolvers += "HMRC Releases" at "https://dl.bintray.com/hmrc/releases"

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin"            % "2.5.19")

// web plugins
addSbtPlugin("com.typesafe.sbt"  % "sbt-coffeescript"      % "1.0.2")
addSbtPlugin("com.typesafe.sbt"  % "sbt-less"              % "1.1.2")
addSbtPlugin("com.typesafe.sbt"  % "sbt-jshint"            % "1.0.6")
addSbtPlugin("com.typesafe.sbt"  % "sbt-rjs"               % "1.0.10")
addSbtPlugin("com.typesafe.sbt"  % "sbt-digest"            % "1.1.4")
addSbtPlugin("com.typesafe.sbt"  % "sbt-mocha"             % "1.1.2")
addSbtPlugin("org.irundaia.sbt"  % "sbt-sassify"           % "1.4.12")

// static analysis
addSbtPlugin("org.scoverage"     % "sbt-scoverage"         % "1.5.1")
addSbtPlugin("org.scalastyle"   %% "scalastyle-sbt-plugin" % "1.0.0")

//Build plugins
addSbtPlugin("uk.gov.hmrc"       % "sbt-auto-build"        % "1.13.0")
addSbtPlugin("uk.gov.hmrc"       % "sbt-git-versioning"    % "1.15.0")
addSbtPlugin("uk.gov.hmrc"       % "sbt-distributables"    % "1.1.0")
addSbtPlugin("uk.gov.hmrc"       % "sbt-artifactory"       % "0.13.0")
