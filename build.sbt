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

import scoverage.ScoverageKeys

val appName = "sm-hub-frontend"

lazy val scoverageSettings = Seq(
  ScoverageKeys.coverageExcludedPackages  := "<empty>;Reverse.*;models/.data/..*;views.*;models.*;filters.*;common.*;.*(AuthService|BuildInfo|Routes).*",
  ScoverageKeys.coverageMinimum           := 80,
  ScoverageKeys.coverageFailOnMinimum     := true,
  ScoverageKeys.coverageHighlighting      := true
)

lazy val frontend = Project(appName, file("."))
  .enablePlugins(PlayScala)
  .settings(PlayKeys.playDefaultPort := 1024)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(scoverageSettings:_*)
  .settings(
    scalaVersion                                  :=  "2.11.11",
    resolvers                                     +=  "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
    Keys.fork                  in IntegrationTest :=  false,
    unmanagedSourceDirectories in IntegrationTest :=  (baseDirectory in IntegrationTest)(base => Seq(base / "it")).value,
    parallelExecution          in IntegrationTest :=  false,
    libraryDependencies                           ++= Seq(
      ws,
      "org.jsoup"              %  "jsoup"              % "1.11.2",
      "org.jsoup"              %  "jsoup"              % "1.11.2" % Test,
      "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1"  % Test,
      "org.mockito"            %  "mockito-core"       % "2.13.0" % Test
    )
  )
