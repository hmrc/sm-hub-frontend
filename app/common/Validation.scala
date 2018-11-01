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

import play.api.Logger
import play.api.data.Forms.text
import play.api.data.format.Formatter
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.data.{FormError, Forms, Mapping}
import play.api.i18n.Messages

import scala.util.{Failure, Success, Try}

object Validation {
  def requiredText(key: String)(implicit messages: Messages): Mapping[String] = {
    val textConstraint: Constraint[String] = Constraint("constraints.text"){
        case "" => Invalid(ValidationError(messages(s"validation.required.$key")))
        case _  => Valid
    }
    text.verifying(textConstraint)
  }

  private[common] def servicesConstraint(key : String)(implicit messages: Messages) = {
    val regex = """^[a-zA-Z0-9_]{1,64}$"""

    val textConstraint: Constraint[String] = Constraint("constraints.text"){ text =>
      if (text.split("\n").toList forall { line =>
        line.trim.matches(regex)
      }) {
        Valid
      } else {
        Invalid(ValidationError(messages(s"validation.required.$key")))
      }
    }

    textConstraint
  }

  def listOfServices(key: String)(implicit messages: Messages): Mapping[String] = {
    text.verifying(servicesConstraint(key))
  }

  private def intFormatter(min: Int, max: Int)(implicit messages: Messages): Formatter[Int] = new Formatter[Int] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Int] = {
      data.get(key) match {
        case Some(possibleInt) => Try(possibleInt.toInt) match {
          case Success(int) => if(int < min | int > max) Left(Seq(FormError(key, messages("validation.required.port")))) else Right(int)
          case Failure(_)   => Left(Seq(FormError(key, messages("validation.required.port"))))
        }
        case None              => Left(Seq(FormError(key, messages("validation.required.port"))))
      }
    }

    override def unbind(key: String, value: Int): Map[String, String] = Map(key -> value.toString)
  }

  def portMapping(implicit messages: Messages): Mapping[Int] = Forms.of[Int](intFormatter(min = 1024, max = 65535))

  def configBuildPort(validPorts: Seq[Int])(implicit messages: Messages): Mapping[Int] = {
    val numberConstraint: Constraint[Int] = Constraint("constraints.number"){
      case taken if validPorts.contains(taken) => Invalid(ValidationError(messages("validation.required.port.in-use", taken.toString)))
      case _                                   => Valid
    }
    portMapping.verifying(numberConstraint)
  }
}
