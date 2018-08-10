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

package connectors

import common.Http
import javax.inject.Inject

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DefaultUpdateConnector @Inject()(val http: Http) extends UpdateConnector {
  override val currentVersion: Option[String] = Option(System.getProperty("currentVersion"))
}

trait UpdateConnector {

  val http: Http

  val currentVersion: Option[String]

  def isUpdateAvailable: Future[Boolean] = {
    http.get("https://api.github.com/repos/hmrc/sm-hub-frontend/releases/latest") map { resp =>
      val latestVersion  = resp.json.\("name").asOpt[String]
      (latestVersion, currentVersion) match {
        case (Some(lv), Some(cv)) => !(lv == cv)
        case (_, _)               => false
      }
    }
  }
}
