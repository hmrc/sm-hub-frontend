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

import java.io.File
import javax.inject.Inject

import forms.{AllProfilesForm, AllServiceForm, AvailablePortsForm, RunningServicesForm, _}
import models.ConfigSetup
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}
import services.SMService
import views.html.pages._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.sys.process._

class DefaultMainController @Inject()(val smService: SMService,
                                      val messagesApi: MessagesApi,
                                      val configuration: Configuration) extends MainController{
  override val githubOrg = configuration.underlying.getString("githubOrg")
}

trait MainController extends Controller with I18nSupport {

  val smService: SMService
  val configuration: Configuration

  val githubOrg: String

  def redirectToRunningServices(): Action[AnyContent] = Action { implicit request =>
    Redirect(routes.MainController.home())
  }

  def home(profile: String, serviceAction: String): Action[AnyContent] = Action.async { implicit request =>
    smService.getRunningServices(profile).map { services =>
      if (!serviceAction.isEmpty) {
        doServiceAction(serviceAction)
        Redirect(routes.MainController.home(profile))
      } else {
        Ok(HomeView(services, RunningServicesForm.form.fill(profile)))
      }
    }
  }

  def doServiceAction(serviceAction: String): Unit = {
    Process(s"sm --${serviceAction} ${configuration.getString("serviceStartMode").getOrElse("")}",
      new File("/"),
      "WORKSPACE" -> configuration.underlying.getString("workspace")).run
  }

  def submitHome(): Action[AnyContent] = Action.async { implicit request =>
    RunningServicesForm.form.bindFromRequest.fold(
      errors => smService.getRunningServices().map(services => BadRequest(HomeView(services, errors))),
      valid  => Future.successful(Redirect(routes.MainController.home(valid)))
    )
  }

  def availablePorts(): Action[AnyContent] = Action { implicit request =>
    Ok(PortsView(Seq.empty, List.empty, AvailablePortsForm.form))
  }

  def submitAvailablePorts(): Action[AnyContent] = Action { implicit request =>
    AvailablePortsForm.form.bindFromRequest.fold(
      errors => BadRequest(PortsView(Seq.empty, List.empty, errors)),
      valid  => valid match {
        case (start, end, Some(amount)) =>
          val consecutivePorts = smService.getConsecutivePorts((start to end).toList, amount)
          Ok(PortsView(Seq.empty, consecutivePorts, AvailablePortsForm.form.fill(valid)))
        case (start, end, _)            =>
          val ports = smService.getValidPortNumbers(Some((start, end)))
          Ok(PortsView(ports, List.empty, AvailablePortsForm.form.fill(valid)))
      }
    )
  }

  def currentProfiles(): Action[AnyContent] = Action { implicit request =>
    val profiles = smService.getAllProfiles
    Ok(ProfilesView(profiles, AllProfilesForm.form))
  }

  def submitCurrentProfiles(): Action[AnyContent] = Action { implicit request =>
    AllProfilesForm.form.bindFromRequest.fold(
      errors => BadRequest(ProfilesView(smService.getAllProfiles, errors)),
      valid  => Ok(ProfilesView(smService.searchForProfile(valid), AllProfilesForm.form.fill(valid)))
    )
  }

  def currentServices(): Action[AnyContent] = Action { implicit request =>
    val services = smService.getAllServices
    Ok(ServicesView(services, AllServiceForm.form))
  }

  def submitCurrentServices(): Action[AnyContent] = Action { implicit request =>
    AllServiceForm.form.bindFromRequest.fold(
      errors => BadRequest(ServicesView(smService.getAllServices, errors)),
      valid => Ok(ServicesView(smService.searchForService(valid), AllServiceForm.form.fill(valid)))
    )
  }

  def servicesInProfile(profile: String): Action[AnyContent] = Action { implicit request =>
    val services = smService.getServicesInProfile(profile)
    Ok(ServicesInProfileView(profile, services))
  }

  def detailsForService(service: String): Action[AnyContent] = Action { implicit request =>
    val serviceDetails = smService.getDetailsForService(service)
    Ok(ServiceDetailsView(serviceDetails))
  }

  def potentialConflicts(): Action[AnyContent] = Action { implicit request =>
    val potentialConflicts = smService.getDuplicatePorts
    Ok(PotentialConflictsView(potentialConflicts))
  }

  def serviceTestRoutes(): Action[AnyContent] = Action { implicit request =>
    val servicesWithTestRoutes = smService.getServicesWithDefinedTestRoutes
    Ok(ServiceTestRoutesView(servicesWithTestRoutes))
  }

  def serviceTestRoutesExpanded(service: String): Action[AnyContent] = Action { implicit request =>
    val serviceTestRoutes = smService.getServicesTestRoutes(service)
    Ok(ServiceTestRoutesExpandedView(service, serviceTestRoutes))
  }

  def availableAssetsVersions(): Action[AnyContent] = Action.async { implicit request =>
    smService.getAssetsFrontendVersions map { versions =>
      Ok(AssetsVersions(versions.reverse))
    }
  }

  def findGHEReferences(): Action[AnyContent] = Action { implicit request =>
    val references = smService.getAllGHERefs
    Ok(GHEReferences(references))
  }

  def showGenerateConfig(): Action[AnyContent] = Action { implicit request =>
    val validPorts = smService.getInUsePorts
    Ok(GenerateConfigView(GenerateConfigForm.form(validPorts)))
  }

  def submitGenerateConfig(): Action[AnyContent] = Action { implicit request =>
    val validPorts = smService.getInUsePorts
    GenerateConfigForm.form(validPorts).bindFromRequest.fold(
      errors => BadRequest(GenerateConfigView(errors)),
      valid  => Ok(GeneratedConfig(Json.prettyPrint(Json.toJson(valid)(ConfigSetup.writes(githubOrg)))))
    )
  }

  def viewServiceLogs(service : String): Action[AnyContent] = Action { implicit request =>
    Ok(ServiceLogsView(smService.retrieveServiceLogs(service), service))
  }
}
