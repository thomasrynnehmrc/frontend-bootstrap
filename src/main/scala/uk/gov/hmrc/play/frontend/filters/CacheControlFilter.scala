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

package uk.gov.hmrc.play.frontend.filters

import play.api.mvc.{Filter, RequestHeader, Result}
import play.api.{Logger, Play}
import play.mvc.Http.Status
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.collection.JavaConverters._
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import scala.concurrent.Future

abstract class CacheControlFilter extends Filter with MicroserviceFilterSupport {
  val cachableContentTypes: Seq[String]

  final def apply(next: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    implicit val hc = HeaderCarrierConverter.fromHeadersAndSession(rh.headers, Some(rh.session))
    next(rh).map(r =>
      (r.header.status, r.body.contentType) match {
        case (Status.NOT_MODIFIED, _)                                                      => r
        case (_, Some(contentType)) if cachableContentTypes.exists(contentType.startsWith) => r
        case _                                                                             => r.withHeaders(CommonHeaders.NoCacheHeader)
    })

  }
}

object CacheControlFilter {
  def fromConfig(configKey: String) =
    new CacheControlFilter {
      override lazy val cachableContentTypes = {
        val c = Play.current.configuration.getStringList(configKey).toList.map(_.asScala).flatten
        Logger.debug(s"Will allow caching of content types matching: ${c.mkString(", ")}")
        c
      }
    }
}
