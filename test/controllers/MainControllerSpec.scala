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

package controllers

import common.RunningResponse
import models.TestRoutesDesc
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.test.FakeRequest
import services.SMService
import play.api.test.Helpers._
import org.mockito.Mockito.{reset, when}
import org.mockito.ArgumentMatchers
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import play.api.Configuration
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.RequestHeader

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class MainControllerSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach {

  val mockSMService     = mock[SMService]
  val mockMessagesApi   = mock[MessagesApi]
  val mockConfiguration = mock[Configuration]

  val request = FakeRequest()

  val testController = new MainController {
    override val configuration = mockConfiguration
    override val githubOrg     = "testGitHubOrg"
    override val messagesApi   = mockMessagesApi
    override val smService     = mockSMService
  }

  val lang = Lang("en")
  val messages = Messages(lang, mockMessagesApi)

  val MOCKED_MESSAGE = "mocked message"

  def mockAllMessages: OngoingStubbing[String] = {
    when(mockMessagesApi.preferred(ArgumentMatchers.any[RequestHeader]()))
      .thenReturn(messages)

    when(mockMessagesApi.apply(ArgumentMatchers.any[String](), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(MOCKED_MESSAGE)

    when(mockMessagesApi.apply(ArgumentMatchers.any[Seq[String]](), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(MOCKED_MESSAGE)
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSMService)
  }

  mockAllMessages

  "redirectToRunningServices" should {
    "return a 303" in {
      val result = testController.redirectToRunningServices()(request)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("/running-services")
    }
  }

  "home" should {
    "return an OK" in {
      when(mockSMService.getRunningServices(ArgumentMatchers.any()))
        .thenReturn(Future.successful(Seq.empty[RunningResponse]))

      val result = testController.home("", "")(request)
      status(result) mustBe OK
    }
  }

  "submitHome" should {
    "return an OK" when {
      "a valid profile has been found and running services have been returned" in {
        val request = FakeRequest().withFormUrlEncodedBody("profile" -> "testProfile")

        val result = testController.submitHome()(request)
        status(result) mustBe SEE_OTHER
      }
    }

    "return a bad request" when {
      "nothing has been put into the form" in {
        when(mockSMService.getRunningServices(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Seq.empty[RunningResponse]))

        val result = testController.submitHome()(request)
        status(result) mustBe BAD_REQUEST
      }
    }
  }

  "availablePorts" should {
    "return an OK" in {
      when(mockSMService.getValidPortNumbers(ArgumentMatchers.any()))
        .thenReturn(Seq.empty[Int])

      val result = testController.availablePorts()(request)
      status(result) mustBe OK
    }
  }

  "submitAvailablePorts" should {
    "return a BadRequest" when {
      "no range has been provided" in {
        when(mockSMService.getValidPortNumbers(ArgumentMatchers.any()))
          .thenReturn(Seq())

        val result = testController.submitAvailablePorts()(request)
        status(result) mustBe BAD_REQUEST
      }
    }

    "return an OK" when {
      "a range has been provided" in {
        val request = FakeRequest().withFormUrlEncodedBody("startPort" -> "1024", "endPort" -> "1030")

        when(mockSMService.getValidPortNumbers(ArgumentMatchers.any()))
          .thenReturn(Seq(1024, 1025, 1026))

        val result = testController.submitAvailablePorts()(request)
        status(result) mustBe OK
      }
    }
  }

  "currentProfiles" should {
    "return an OK" in {
      when(mockSMService.getAllProfiles)
        .thenReturn(Seq("testProfile1", "testProfile2"))

      val result = testController.currentProfiles()(request)
      status(result) mustBe OK
    }
  }

  "submitCurrentProfiles" should {
    "return an OK" in {
      when(mockSMService.searchForProfile(ArgumentMatchers.any()))
        .thenReturn(Seq("testProfile1"))

      val result = testController.submitCurrentProfiles()(request.withFormUrlEncodedBody("profiles" -> "testProfile1"))
      status(result) mustBe OK
    }

    "return a BadRequest" in {
      when(mockSMService.getAllProfiles)
        .thenReturn(Seq("testProfile1", "testProfile2"))

      val result = testController.submitCurrentProfiles()(request.withFormUrlEncodedBody())
      status(result) mustBe BAD_REQUEST
    }
  }

  "currentServices" should {
    "return an OK" in {
      when(mockSMService.getAllServices)
        .thenReturn(Seq("testService1", "testService2"))

      val result = testController.currentServices()(request)
      status(result) mustBe OK
    }
  }

  "submitCurrentServices" should {
    "return an OK" in {
      when(mockSMService.searchForService(ArgumentMatchers.any()))
        .thenReturn(Seq("testService1"))

      val result = testController.submitCurrentServices()(request.withFormUrlEncodedBody("service" -> "testService1"))
      status(result) mustBe OK
    }

    "return a BadRequest" in {
      when(mockSMService.getAllServices)
        .thenReturn(Seq("testService1", "testService2"))

      val result = testController.submitCurrentServices()(request.withFormUrlEncodedBody())
      status(result) mustBe BAD_REQUEST
    }
  }

  "servicesInProfile" should {
    "return an OK" in {
      when(mockSMService.getServicesInProfile(ArgumentMatchers.any()))
        .thenReturn(Seq("testService1", "testService2"))

      val result = testController.servicesInProfile("testProfile")(request)
      status(result) mustBe OK
    }
  }

  "detailsForService" should {
    "return an OK" in {
      when(mockSMService.getDetailsForService(ArgumentMatchers.any()))
        .thenReturn(Json.obj(
          "defaultPort" -> 1024,
          "name"        -> "testName",
          "frontend"    -> true,
          "template"    -> "testTemplate",
          "location"    -> "testLocation",
          "sources"     -> Json.obj(
            "repo" -> "/test/repo"
          ),
          "binary" -> Json.obj(
            "cmd" -> Json.arr("1")
          )
        ))

      val result = testController.detailsForService("testService")(request)
      status(result) mustBe OK
    }
  }

  "potentialConflicts" should {
    "return an OK" in {
      when(mockSMService.getDuplicatePorts)
        .thenReturn(Map(1024 -> Seq("testService1", "testService2")))

      val result = testController.potentialConflicts()(request)
      status(result) mustBe OK
    }
  }

  "serviceTestRoutes" should {
    "return an OK" in {
      when(mockSMService.getServicesWithDefinedTestRoutes)
        .thenReturn(Seq("testService1", "testService2"))

      val result = testController.serviceTestRoutes()(request)
      status(result) mustBe OK
    }
  }

  "serviceTestRoutesExpanded" should {
    "return an OK" in {
      when(mockSMService.getServicesTestRoutes(ArgumentMatchers.any()))
        .thenReturn(Some(Seq(TestRoutesDesc("testName", "/test/uri", "testDesc"))))

      val result = testController.serviceTestRoutesExpanded("testService1")(request)
      status(result) mustBe OK
    }
  }

  "availableAssetsVersions" should {
    "return an OK" in {
      when(mockSMService.getAssetsFrontendVersions)
        .thenReturn(Future(List("version1", "version2", "version3")))

      val result = testController.availableAssetsVersions()(request)
      status(result) mustBe OK
    }
  }

  "findGHEReferences" should {
    "return an OK" in {
      when(mockSMService.getAllGHERefs)
        .thenReturn(Seq(("testService1", "repo")))

      val result = testController.findGHEReferences()(request)
      status(result) mustBe OK
    }
  }

  "showGenerateConfig" should {
    "return an Ok" in {
      when(mockSMService.getInUsePorts)
        .thenReturn(Seq(1024, 1025, 1026))

      val result = testController.showGenerateConfig()(request)
      status(result) mustBe OK
    }
  }

  "submitGenerateConfig" should {
    "return an Ok" in {
      val formRequest = request.withFormUrlEncodedBody(
        "name"                 -> "testName",
        "template"             -> "play",
        "defaultPort"          -> "2015",
        "hasMongo"             -> "true",
        "binary.groupId"       -> "testGroupId",
        "binary.nexus"         -> "testNexus",
        "binary.cmd"           -> "testCmd"
      )

      when(mockSMService.getInUsePorts)
        .thenReturn(Seq(1024, 1025, 1026))

      val result = testController.submitGenerateConfig()(formRequest)
      status(result) mustBe OK
    }

    "return a BadRequest" in {
      when(mockSMService.getInUsePorts)
        .thenReturn(Seq(1024, 1025, 1026))

      val result = testController.submitGenerateConfig()(request)
      status(result) mustBe BAD_REQUEST
    }
  }

  "viewServiceLogs" should {
    "return an Ok on a valid service" in {
      when(mockSMService.retrieveServiceLogs("service"))
        .thenReturn("TEST_LOG")

      val result = testController.viewServiceLogs("service")(request)
      status(result) mustBe OK
      contentAsString(result) must include("TEST_LOG")
    }
  }
}
