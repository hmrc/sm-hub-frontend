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

package services

import java.net.ConnectException

import common.{AmberResponse, GreenResponse, RedResponse}
import connectors.{HttpConnector, JsonConnector}
import models.TestRoutesDesc
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.data.validation.ValidationError
import play.api.libs.json._
import play.api.test.Helpers._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class SMServiceSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach {

  val mockHttpConnector = mock[HttpConnector]
  val mockJsonConnector = mock[JsonConnector]

  val testService = new SMService {
    override val httpConnector = mockHttpConnector
    override val jsonConnector = mockJsonConnector
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      mockJsonConnector,
      mockHttpConnector
    )
  }

  val profilesJson = Json.obj(
    "ALL"          -> Json.arr("testService1", "testService2", "testService3", "testService4"),
    "TESTPROFILE1" -> Json.arr("testService1", "testService2", "testService3"),
    "TESTPROFILE2" -> Json.arr("1", "1")
  )

  val servicesJson1000 = (2000 to 3000).toList.filterNot(_ == 2001).map(i => Json.obj(s"testService$i" -> Json.obj("defaultPort" -> i))).fold(Json.obj())((a,b) => (a ++ b))

  val servicesJson = Json.obj(
    "testService1" -> Json.obj("defaultPort" -> 1024),
    "testService2" -> Json.obj(
      "defaultPort" -> 1025,
      "healthcheck" -> Json.obj(
        "url" -> "/test"
      )
    ),
    "testService3" -> Json.obj("defaultPort" -> 1026),
    "testService4" -> Json.obj(
      "defaultPort" -> 1026,
      "testRoutes"  -> Json.arr(
        Json.obj(
          "name"        -> "testRoute",
          "route"       -> "/test/uri",
          "description" -> "testDesc"
        )
      )
    )
  )

  "getRunningServices" should {
    "return an empty seq" when {
      "no profile is provided" in {
        assert(await(testService.getRunningServices()).isEmpty)
      }

      "no profile is found" in {
        assert(await(testService.getRunningServices(currentProfile = "test-profile-not-found")).isEmpty)
      }
    }

    "return a sequence of String -> RunningResponses" in {
      when(mockJsonConnector.loadProfilesJson)
        .thenReturn(profilesJson)

      when(mockJsonConnector.loadServicesJson)
        .thenReturn(servicesJson)

      when(mockHttpConnector.pingService(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(
          Future(GreenResponse("testService1",1024)),
          Future(AmberResponse("testService2",1025)),
          Future(RedResponse("testService",1026))
        )

      val result = testService.getRunningServices(currentProfile = "testProfile1")
      await(result) mustBe Seq(GreenResponse("testService1",1024),AmberResponse("testService2",1025),RedResponse("testService",1026))
    }
  }

  "getValidPortNumbers" should {
    "return an empty seq" when {
      "no range is provided" in {
        testService.getValidPortNumbers(None) mustBe Seq()
      }
    }

    "return a seq of ints" when {
      "a range has been provided" in {
        when(mockJsonConnector.loadServicesJson)
          .thenReturn(servicesJson)

        val result = testService.getValidPortNumbers(Some(1024 -> 1030))
        result mustBe Seq(1027, 1028, 1029, 1030)
      }
      "a range has been provided of exactly 1000 difference and no ports exist in that range from loadServicesJson" in {
        when(mockJsonConnector.loadServicesJson)
          .thenReturn(servicesJson)

        val result = testService.getValidPortNumbers(Some(8000 -> 9000))
        result mustBe (8000 to 9000).toList
        result.size mustBe 1001
      }
      "a range has been provided of exactly 1000 difference but 1000 ports exist within that range from loadServicesJson so one remains free" in {
        when(mockJsonConnector.loadServicesJson)
          .thenReturn(servicesJson1000)
        val result = testService.getValidPortNumbers(Some(2000 -> 3000))
        result.size mustBe 1
      }
    }
  }

  "getInUsePorts" should {
    "return a populated seq of ports" in {
      when(mockJsonConnector.loadServicesJson)
        .thenReturn(servicesJson)

      val result = testService.getInUsePorts
      result mustBe Seq(1024, 1025, 1026, 1026)
    }

    "return an empty seq" in {
      when(mockJsonConnector.loadServicesJson)
        .thenReturn(Json.obj())

      val result = testService.getInUsePorts
      result mustBe Seq()
    }
  }

  "getAllProfiles" should {
    "return a seq of string" in {
      when(mockJsonConnector.loadProfilesJson)
        .thenReturn(profilesJson)

      testService.getAllProfiles mustBe Seq("TESTPROFILE1", "TESTPROFILE2")
    }
  }

  "getAllServices" should {
    "return all services in a seq" in {
      when(mockJsonConnector.loadServicesJson)
        .thenReturn(servicesJson)

      testService.getAllServices mustBe Seq("testService1", "testService2", "testService3", "testService4")
    }
  }

  "searchForService" should {
    val searchJson = Json.obj(
      "cars"    -> Json.obj(),
      "bars"    -> Json.obj(),
      "scars"   -> Json.obj(),
      "sabars"  -> Json.obj(),
      "chicken" -> Json.obj(),
      "burger"  -> Json.obj()
    )

    "return a seq of services (cars)" in {
      when(mockJsonConnector.loadServicesJson)
        .thenReturn(searchJson)

      val result = testService.searchForService("cars")
      result mustBe Seq("cars", "scars")
    }

    "return a seq of services (bars)" in {
      when(mockJsonConnector.loadServicesJson)
        .thenReturn(searchJson)

      val result = testService.searchForService("bars")
      result mustBe Seq("bars", "sabars")
    }

    "return a seq of services (rs)" in {
      when(mockJsonConnector.loadServicesJson)
        .thenReturn(searchJson)

      val result = testService.searchForService("rs")
      result mustBe Seq("cars", "bars", "scars", "sabars")
    }

    "return a seq of services (urg)" in {
      when(mockJsonConnector.loadServicesJson)
        .thenReturn(searchJson)

      val result = testService.searchForService("urg")
      result mustBe Seq("burger")
    }

    "return a seq of services (chi)" in {
      when(mockJsonConnector.loadServicesJson)
        .thenReturn(searchJson)

      val result = testService.searchForService("chi")
      result mustBe Seq("chicken")
    }

    "return an empty seq if no services are found" in {
      when(mockJsonConnector.loadServicesJson)
        .thenReturn(searchJson)

      val result = testService.searchForService("qwerty")
      result mustBe Seq()
    }

    "return only cars if searching for exact match" in {
      when(mockJsonConnector.loadServicesJson)
        .thenReturn(searchJson)

      val result = testService.searchForService(""" "cars" """)
      result mustBe Seq("cars")
    }
  }

  "searchForProfile" should {
    val searchJson = Json.obj(
      "cars"    -> Json.arr(),
      "bars"    -> Json.arr(),
      "scars"   -> Json.arr(),
      "sabars"  -> Json.arr(),
      "chicken" -> Json.arr(),
      "burger"  -> Json.arr()
    )

    "return a seq of services (cars)" in {
      when(mockJsonConnector.loadProfilesJson)
        .thenReturn(searchJson)

      val result = testService.searchForProfile("cars")
      result mustBe Seq("cars", "scars")
    }

    "return a seq of services (bars)" in {
      when(mockJsonConnector.loadProfilesJson)
        .thenReturn(searchJson)

      val result = testService.searchForProfile("bars")
      result mustBe Seq("bars", "sabars")
    }

    "return a seq of services (rs)" in {
      when(mockJsonConnector.loadProfilesJson)
        .thenReturn(searchJson)

      val result = testService.searchForProfile("rs")
      result mustBe Seq("cars", "bars", "scars", "sabars")
    }

    "return a seq of services (urg)" in {
      when(mockJsonConnector.loadProfilesJson)
        .thenReturn(searchJson)

      val result = testService.searchForProfile("urg")
      result mustBe Seq("burger")
    }

    "return a seq of services (chi)" in {
      when(mockJsonConnector.loadProfilesJson)
        .thenReturn(searchJson)

      val result = testService.searchForProfile("chi")
      result mustBe Seq("chicken")
    }

    "return a seq of services (chicken)" in {
      when(mockJsonConnector.loadProfilesJson)
        .thenReturn(searchJson)

      val result = testService.searchForProfile(""""chicken"""")
      result mustBe Seq("chicken")
    }

    "return an empty seq if no services are found" in {

      when(mockJsonConnector.loadProfilesJson)
        .thenReturn(searchJson)

      val result = testService.searchForProfile("qwerty")
      result mustBe Seq()
    }
  }

  "getServicesInProfile" should {
    "return all services in a profile in a seq" in {
      when(mockJsonConnector.loadProfilesJson)
        .thenReturn(profilesJson)

      testService.getServicesInProfile("testprofile1") mustBe Seq("testService1", "testService2", "testService3")
    }
  }

  "getDetailsForService" should {
    "return the defined details for the service" in {
      when(mockJsonConnector.loadServicesJson)
        .thenReturn(servicesJson)

      testService.getDetailsForService("testService1") mustBe Json.obj("defaultPort" -> 1024)
    }
  }

  "getDuplicatePorts" should {
    "return a map of ports to services" in {
      when(mockJsonConnector.loadServicesJson)
        .thenReturn(servicesJson)

      testService.getDuplicatePorts mustBe Map(1026 -> Seq("testService3", "testService4"))
    }
  }

  "getServicesWithDefinedTestRoutes" should {
    "return a seq of test routes for a service" in {
      when(mockJsonConnector.loadServicesJson)
        .thenReturn(servicesJson)

      testService.getServicesWithDefinedTestRoutes mustBe Seq("testService4")
    }
  }

  "getServicesTestRoutes" should {
    "return a defined seq of test routes" in {
      when(mockJsonConnector.loadServicesJson)
        .thenReturn(servicesJson)

      val result = testService.getServicesTestRoutes("testService4")
      result mustBe Some(Seq(TestRoutesDesc(name = "testRoute", route = "http://localhost:1026/test/uri", description = "testDesc")))
    }

    "return an None" in {
      when(mockJsonConnector.loadServicesJson)
        .thenReturn(servicesJson)

      val result = testService.getServicesTestRoutes("testService3")
      result mustBe None
    }
  }

  "getAssetsFrontendVersions" should {
    "return list of <li with href of specific assets version>" in {
      val result = Seq("2.214.0", "2.211.0")

      val htmlResponse =
        """
          |<body>
          |<h2>Directory listing for /assets/</h2>
          |<hr>
          |<ul>
          |<li><a href="2.214.0">2.214.0/</a>
          |<li><a href="2.211.0">2.211.0/</a>
          |</ul>
          |<hr>
          |</body>
        """.stripMargin

      when(mockHttpConnector.getBodyOfPage(ArgumentMatchers.any(),ArgumentMatchers.any()))
          .thenReturn(Future.successful(Jsoup.parse(htmlResponse)))

      await(testService.getAssetsFrontendVersions) mustBe result
    }

    "return an empty list when a ConnectException is thrown" in {
      when(mockHttpConnector.getBodyOfPage(ArgumentMatchers.any(),ArgumentMatchers.any()))
        .thenReturn(Future.failed(new ConnectException()))

      await(testService.getAssetsFrontendVersions) mustBe List()
    }
  }

  "getAllGHERefs" should {
    "return a list of GHE refs" in {
      val servicesJson = Json.obj(
        "testService1" -> Json.obj("sources" -> Json.obj("repo" -> "github.tools")),
        "testService2" -> Json.obj("sources" -> Json.obj("repo" -> "github"))
      )

      when(mockJsonConnector.loadServicesJson)
        .thenReturn(servicesJson)

      val result = testService.getAllGHERefs
      result mustBe List(("testService1", "github.tools"))
    }

    "return an empty list" in {
      val servicesJson = Json.obj(
        "testService1" -> Json.obj("sources" -> Json.obj("repo" -> "github")),
        "testService2" -> Json.obj("sources" -> Json.obj("repo" -> "github"))
      )

      when(mockJsonConnector.loadServicesJson)
        .thenReturn(servicesJson)

      val result = testService.getAllGHERefs
      result mustBe List()
    }
  }

  "updateProfilesConfig" should {
    "update a profile" when {
      "all the services exist" in {
        val profile = "TESTPROFILE1"
        val newService = "testService3"

        when(mockJsonConnector.loadProfilesJson)
          .thenReturn(profilesJson)
        when(mockJsonConnector.loadServicesJson)
          .thenReturn(servicesJson)

        testService.upsertProfileServices(profile, List(newService)) mustBe None
      }
    }

    "create a profile" when {
      "it does not exist" in {
        val profile = "PROFILE_IN_QUESTION"
        val newService = "testService3"

        when(mockJsonConnector.loadProfilesJson)
          .thenReturn(profilesJson)

        testService.upsertProfileServices(profile, List(newService)) mustBe None
      }
    }

    "fail to update a profile" when {
      "one of the new services does not exist" in {
        val profile = "TESTPROFILE1"
        val newService = "testService3thatdoesnotexist"

        when(mockJsonConnector.loadProfilesJson)
          .thenReturn(profilesJson)
        when(mockJsonConnector.loadServicesJson)
          .thenReturn(servicesJson)

        testService.upsertProfileServices(profile, List(newService)) mustBe Some(newService)
      }
    }
  }
}
