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

package common

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.data.validation.{Invalid, Valid, ValidationError, ValidationResult}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.RequestHeader

class ValidationSpec extends PlaySpec with MockitoSugar {

  val validation: Validation.type = Validation
  val mockMessagesApi: MessagesApi = mock[MessagesApi]

  private def formBuilder(serviceList : List[String]): String = serviceList.mkString("\n")

  val lang = Lang("en")
  implicit val messages: Messages = Messages(lang, mockMessagesApi)

  val MOCKED_MESSAGE = "mocked message"

  def mockAllMessages: OngoingStubbing[String] = {
    when(mockMessagesApi.preferred(ArgumentMatchers.any[RequestHeader]()))
      .thenReturn(messages)

    when(mockMessagesApi.apply(ArgumentMatchers.any[String](), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(MOCKED_MESSAGE)

    when(mockMessagesApi.apply(ArgumentMatchers.any[Seq[String]](), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(MOCKED_MESSAGE)
  }

  mockAllMessages

  "servicesConstraint" should {
    "validate a list of services successfully" when {
      "there is just one service" in {
        val services = List("TEST")
        val formBinding: ValidationResult = validation.servicesConstraint("test-key").apply(formBuilder(services))

        formBinding mustBe Valid
      }

      "there are three services, with full regex parameters" in {
        val services = List("TEST_SERVICE", "lowerCase9_A", "UPPER_31GENERIC")
        val formBinding: ValidationResult = validation.servicesConstraint("test-key").apply(formBuilder(services))

        formBinding mustBe Valid
      }
    }

    "do not accept a list of services" when {
      "there are no services" in {
        val services = List()
        val formBinding: ValidationResult = validation.servicesConstraint("test-key").apply(formBuilder(services))

        val Invalid(list) = formBinding
        list mustBe List(ValidationError(MOCKED_MESSAGE))
      }

      "there is a service with invalid characters" in {
        val services = List("TEST{}{}{")
        val formBinding: ValidationResult = validation.servicesConstraint("test-key").apply(formBuilder(services))

        val Invalid(list) = formBinding
        list mustBe List(ValidationError(MOCKED_MESSAGE))
      }
    }
  }

}
