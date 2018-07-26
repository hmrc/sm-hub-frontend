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

package filters

import akka.stream.Materializer
import common.Logging
import connectors.UpdateConnector
import javax.inject.Inject
import play.api.mvc.{Filter, RequestHeader, Result}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

class DefaultUpdateFilter @Inject()(implicit val mat: Materializer,
                                    val updateConnector: UpdateConnector) extends UpdateFilter

trait UpdateFilter extends Filter with Logging {

  val updateConnector: UpdateConnector

  override def apply(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {
    if(rh.uri.contains("assets")) {
      f(rh)
    } else {
      logger.info("fetching latest version from github")
      for {
        update <- updateConnector.isUpdateAvailable
        result <- f(rh.copy(headers = rh.headers.add("update" -> update.toString)))
      } yield result
    }
  }
}
