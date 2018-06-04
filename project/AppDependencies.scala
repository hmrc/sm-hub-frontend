import play.sbt.PlayImport.ws
import sbt._

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

object AppDependencies {
  def apply(): Seq[ModuleID] = MainDependencies() ++ TestDependencies()
}

private object MainDependencies extends Versions {

  private val playDependencies: Seq[ModuleID] = Seq(ws)

  private val dependencies: Seq[ModuleID] = Seq(
    "org.jsoup" % "jsoup" % jsoupVersion
  )

  def apply(): Seq[ModuleID] = dependencies ++ playDependencies
}

private object TestDependencies extends Versions {
  private val testDependencies: Seq[ModuleID] = Seq(
    "org.jsoup"              %  "jsoup"              % jsoupVersion       % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlay  % Test,
    "org.mockito"            %  "mockito-core"       % mockitoVersion     % Test,
    "org.pegdown"            %  "pegdown"            % pegdownVersion     % Test
  )

  def apply(): Seq[ModuleID] = testDependencies
}

trait Versions {
  val jsoupVersion      = "1.11.2"
  val scalaTestPlusPlay = "2.0.1"
  val mockitoVersion    = "2.13.0"
  val pegdownVersion    = "1.6.0"
}
