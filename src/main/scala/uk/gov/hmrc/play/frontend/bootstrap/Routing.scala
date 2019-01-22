/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.play.frontend.bootstrap

import play.Logger
import play.api.mvc.{Handler, RequestHeader}
import play.api.{Application, GlobalSettings}

import scala.util.matching.Regex

object Routing {

  // Play 2.0 doesn't support trailing slash: http://play.lighthouseapp.com/projects/82401/tickets/98
  trait RemovingOfTrailingSlashes extends GlobalSettings {

    override def onRouteRequest(request: RequestHeader): Option[Handler] = super.onRouteRequest(request).orElse {
      Some(request.path).filter(_.endsWith("/")).flatMap(p => super.onRouteRequest(request.copy(path = p.dropRight(1))))
    }
  }

  trait BlockingOfPaths extends GlobalSettings {

    def blockedPathPattern: Option[Regex] = None

    override def onStart(app: Application) {
      super.onStart(app)
      Logger.info(blockedPathPattern.fold(s"No requests will be blocked based on their path")(p =>
        s"Any requests with paths that match $p will be blocked"))
    }

    override def onRouteRequest(request: RequestHeader): Option[Handler] = blockedPathPattern match {
      case Some(isBlockedPath) =>
        request.path match {
          case isBlockedPath() =>
            Logger.debug(s"Blocked request for ${request.path} as it matches $isBlockedPath")
            None
          case isNotBlockedPath => super.onRouteRequest(request)
        }
      case None => super.onRouteRequest(request)
    }
  }
}
