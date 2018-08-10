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

import com.google.inject.AbstractModule
import connectors._
import controllers.{DefaultMainController, MainController}
import filters.{DefaultRequestLoggingFilter, DefaultUpdateFilter, RequestLoggingFilter, UpdateFilter}
import services.{DefaultSMService, SMService}

class FrontendModule extends AbstractModule {
  override def configure(): Unit = {
    bindCommon()
    bindConnectors()
    bindServices()
    bindControllers()
  }

  def bindCommon(): Unit = {
    bind(classOf[Http]).to(classOf[DefaultHttp]).asEagerSingleton()
    bind(classOf[RequestLoggingFilter]).to(classOf[DefaultRequestLoggingFilter]).asEagerSingleton()
    bind(classOf[UpdateFilter]).to(classOf[DefaultUpdateFilter]).asEagerSingleton()
    bind(classOf[ErrorHandler]).to(classOf[DefaultErrorHandler]).asEagerSingleton()
  }

  def bindConnectors(): Unit = {
    bind(classOf[JsonConnector]).to(classOf[DefaultJsonConnector]).asEagerSingleton()
    bind(classOf[HttpConnector]).to(classOf[DefaultHttpConnector]).asEagerSingleton()
    bind(classOf[UpdateConnector]).to(classOf[DefaultUpdateConnector]).asEagerSingleton()
  }

  def bindServices(): Unit = {
    bind(classOf[SMService]).to(classOf[DefaultSMService]).asEagerSingleton()
  }

  def bindControllers(): Unit = {
    bind(classOf[MainController]).to(classOf[DefaultMainController]).asEagerSingleton()
  }
}
