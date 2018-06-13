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

import common.{Logging, RunningResponse}
import connectors.{HttpConnector, JsonConnector}
import javax.inject.Inject
import models.TestRoutesDesc
import play.api.Configuration
import play.api.libs.json.{JsObject, JsValue}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.sys.process.Process
import scala.util.Try

class DefaultSMService @Inject()(val jsonConnector: JsonConnector,
                                 val httpConnector: HttpConnector,
                                 config: Configuration) extends SMService

trait SMService extends Logging {
  val jsonConnector: JsonConnector
  val httpConnector: HttpConnector

  private val portRange = 1025 to 65535

  def getRunningServices(currentProfile: String = ""): Future[Seq[RunningResponse]] = {
    val profile = currentProfile.trim.replace(" ", "_")
    if (profile.isEmpty) { Future.successful(Seq.empty) } else {
      jsonConnector.loadProfilesJson.\(profile.toUpperCase).asOpt[Set[String]].fold[Future[Seq[RunningResponse]]](Future.successful(Seq.empty)){
        validServices =>
          pingMultipleServices(jsonConnector.loadServicesJson.fields.collect {
            case (name,json) if validServices(name) =>
              val port = json.\("defaultPort").as[Int]
              json.\("healthcheck").\("url").asOpt[String]
                .fold((name, s"http://localhost:$port/ping/ping", port))(url =>
                (name, url.replace("${port}", port.toString), port))
        })
      }
    }
  }

  private def pingMultipleServices(services: Seq[(String, String, Int)]): Future[Seq[RunningResponse]] = {
    Future.sequence(services map {
      case (name, url, port) => httpConnector.pingService(name, url, port)
    })
  }

  def getValidPortNumbers(searchedRange: Option[(Int, Int)]): Seq[Int] = {
    searchedRange match {
      case Some((start, end)) =>
        val currentPorts   = jsonConnector.loadServicesJson.fields map { case (_, details) => details.\("defaultPort").asOpt[Int] }
        val availablePorts = portRange.diff(currentPorts.flatten).toSet
        (start to end).filter(availablePorts)
      case _ => Seq.empty
    }
  }

  def getInUsePorts: Seq[Int] = {
    val currentPorts = jsonConnector.loadServicesJson.fields map { case (_, details) => details.\("defaultPort").asOpt[Int] }
    currentPorts.flatten
  }

  def getAllProfiles: Seq[String] = {
    jsonConnector.loadProfilesJson.fields collect { case (service, _) if service != "ALL" => service }
  }

  def searchForProfile(query: String): Seq[String] = {
    if(query.contains("\"")) {
      exactMatchSearch(jsonConnector.loadProfilesJson.fields, query)
    } else {
      jsonConnector.loadProfilesJson.fields.collect { case (name, _) if name.contains(query) => name}
    }
  }

  def getAllServices: Seq[String] = {
    jsonConnector.loadServicesJson.fields map { case (service, _) => service}
  }

  def searchForService(query: String): Seq[String] = {
    val servicesJson: Seq[(String, JsValue)] = jsonConnector.loadServicesJson.fields

    (isAnInt(query), query.contains("\"")) match {
      case (true, _) => searchServicesUsingPort(query, servicesJson)
      case (_, true) => exactMatchSearch(servicesJson, query.toUpperCase)
      case _ => servicesJson.collect { case (name, _) if name.contains(query.toUpperCase) => name }
    }
  }

  private def isAnInt(query: String): Boolean = Try(query.toInt).isSuccess

  private def searchServicesUsingPort(query: String, servicesJson: => Seq[(String, JsValue)]): Seq[String] =
    servicesJson
      .filter { case (_, js) => js.\("defaultPort").asOpt[Int].isDefined }
      .collect { case (name, js) if js.\("defaultPort").as[Int].toString.contains(query) => name }

  private def exactMatchSearch(f: => Seq[(String, JsValue)], quey: String): Seq[String] = {
    val query = quey.trim.replaceAll("\"", "")
    f.collect { case (name, _) if name.split("_").contains(query) => name }
  }

  def getServicesInProfile(profile: String): Seq[String] = {
    jsonConnector.loadProfilesJson.\(profile.toUpperCase).as[Seq[String]]
  }

  def getDetailsForService(service: String): JsObject = {
    jsonConnector.loadServicesJson.\(service).as[JsObject]
  }

  def getDuplicatePorts: Map[Int, Seq[String]] = {
    val validServices = jsonConnector.loadServicesJson.fields
      .filter{ case (_, js) => js.\("defaultPort").asOpt[Int].isDefined }

    val allPorts = validServices.map {
      case(_, js) => js.\("defaultPort").as[Int]
    }
    val duplicatePorts = allPorts.diff(allPorts.distinct)

    validServices
      .map{case (service, json) => service -> json.\("defaultPort").as[Int]}
      .filter{case (_, port) => duplicatePorts.contains(port)}
      .groupBy{case (_, port) => port}
      .map{ case (port, services) => port -> services.map { case (name, _) => name } }
  }

  def getServicesWithDefinedTestRoutes: Seq[String] = {
    jsonConnector.loadServicesJson.fields collect {
      case (service, js) if js.\("testRoutes").asOpt[JsValue].isDefined => service
    }
  }

  def getServicesTestRoutes(service: String): Option[Seq[TestRoutesDesc]] = {
    val details = getDetailsForService(service)
    jsonConnector.loadServicesJson.fields
      .collect { case (name, js) if name == service => js.\("testRoutes").asOpt[Seq[TestRoutesDesc]] }
      .head
      .map(testRoutes => testRoutes.map { testRoute => testRoute.copy(route = s"http://localhost:${details.\("defaultPort").as[Int]}${testRoute.route}") })
  }

  def getAssetsFrontendVersions: Future[List[String]] = {
    httpConnector.getBodyOfPage(9032, "/assets") map { doc =>
      doc.getElementsByTag("a").text.replace("/", "").split(" ").toList
    } recover {
      case _ =>
        logger.warn("Assets frontend currently isn't available on port 9032")
        List.empty[String]
    }
  }

  def getAllGHERefs: Seq[(String, String)] = {
    jsonConnector.loadServicesJson.fields
      .filter{ case(_, js) => js.\("sources").asOpt[JsObject].isDefined }
      .collect {
        case (service, js) if js.\("sources").\("repo").as[String].contains("tools") =>
          service -> js.\("sources").\("repo").as[String]
      }
  }

  def retrieveServiceLogs(service : String): String = {
    Process(s"sm -l $service").!!.takeRight(100000).trim
  }
}
