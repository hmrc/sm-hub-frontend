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

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsObject, Json}

class JsonConnectorSpec extends PlaySpec {

  val servicesJson = """
                       |{
                       |  "testService1" : {},
                       |  "testService2" : {},
                       |  "testService3" : {}
                       |}
                     """.stripMargin

  val profilesJson = """
                       |{
                       |  "testProfile1" : [],
                       |  "testProfile2" : [],
                       |  "testProfile3" : []
                       |}
                     """.stripMargin

  def testConnector(jsonFullOfServices: String = servicesJson):JsonConnector = new JsonConnector {
    override def sourceFileJson(fileName: String): String = {
      fileName match {
        case "services" => jsonFullOfServices
        case "profiles" => profilesJson
      }
    }
  }

  "loadServicesJson" should {
    "return a json obj" in {
      val res = testConnector().loadServicesJson
      res mustBe Json.parse(servicesJson).as[JsObject]
      res.value.size mustBe 3
    }
    "return a json obj with 1000 entries" in {
      val servicesJson1000 = (2000 to 3000).toList.map(i => Json.obj(s"testService$i" -> Json.obj("defaultPort" -> i))).fold(Json.obj())((a,b) => (a ++ b))
      val res = testConnector(servicesJson1000.toString()).loadServicesJson
      res mustBe servicesJson1000
      res.value.size mustBe 1001
    }
    "return a json obj with a random amount of entries" in {
      val services = (2000 to 2024).toList.map(i => Json.obj(s"testService$i" -> Json.obj("defaultPort" -> i))).fold(Json.obj())((a,b) => (a ++ b))
      val res = testConnector(services.toString()).loadServicesJson
      res mustBe services
      res.value.size mustBe 25
    }
  }

  "loadProfilesJson" should {
    "return a json obj" in {
      testConnector().loadProfilesJson mustBe Json.parse(profilesJson).as[JsObject]
    }
  }
}
