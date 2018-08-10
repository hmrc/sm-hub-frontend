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
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSResponse
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class UpdateConnectorSpec extends PlaySpec with MockitoSugar with FutureAwaits with DefaultAwaitTimeout with BeforeAndAfterEach {

  val mockHttpClient = mock[Http]

  def testConnector(currentVer: Option[String]): UpdateConnector = new UpdateConnector {
    override val http           = mockHttpClient
    override val currentVersion = currentVer
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockHttpClient)
  }

  def fakeResponse(statusInt: Int, bodyIn: JsValue): WSResponse = new WSResponse {
    override def cookie(name: String) = ???
    override def underlying[T]        = ???
    override def body                 = ???
    override def bodyAsBytes          = ???
    override def cookies              = ???
    override def allHeaders           = ???
    override def xml                  = ???
    override def statusText           = ???
    override def json                 = bodyIn
    override def header(key: String)  = ???
    override def status               = statusInt
  }

  val OK = 200

  "isUpdateAvailable" should {
    "return true" when {
      "the current version and latest version don't match" in {
        when(mockHttpClient.get(ArgumentMatchers.any()))
          .thenReturn(Future(fakeResponse(OK, Json.parse("""{ "name" : "0.6.0" }"""))))

        val result = await(testConnector(Some("0.3.0")).isUpdateAvailable)
        assert(result)
      }
    }

    "return false" when {
      "the current version and latest version do match" in {
        when(mockHttpClient.get(ArgumentMatchers.any()))
          .thenReturn(Future(fakeResponse(OK, Json.parse("""{ "name" : "0.6.0" }"""))))

        val result = await(testConnector(Some("0.6.0")).isUpdateAvailable)
        assert(!result)
      }

      "the current version couldn't be pulled from the config" in {
        when(mockHttpClient.get(ArgumentMatchers.any()))
          .thenReturn(Future(fakeResponse(OK, Json.parse("""{ "name" : "0.6.0" }"""))))

        val result = await(testConnector(None).isUpdateAvailable)
        assert(!result)
      }

      "the latest version couldn't be pulled from git" in {
        when(mockHttpClient.get(ArgumentMatchers.any()))
          .thenReturn(Future(fakeResponse(OK, Json.parse("""{ "abc" : "xyz" }"""))))

        val result = await(testConnector(Some("0.3.0")).isUpdateAvailable)
        assert(!result)
      }

      "neither the current or latest version could be pulled" in {
        when(mockHttpClient.get(ArgumentMatchers.any()))
          .thenReturn(Future(fakeResponse(OK, Json.parse("""{ "abc" : "xyz" }"""))))

        val result = await(testConnector(None).isUpdateAvailable)
        assert(!result)
      }
    }
  }
}
