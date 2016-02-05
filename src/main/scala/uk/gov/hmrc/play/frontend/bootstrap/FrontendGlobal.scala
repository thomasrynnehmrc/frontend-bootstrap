/*
 * Copyright 2016 HM Revenue & Customs
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

import com.kenshoo.play.metrics.MetricsFilter
import org.apache.commons.codec.binary.Base64
import play.api._
import play.api.mvc._
import play.filters.csrf.CSRFFilter
import play.filters.headers.{DefaultSecurityHeadersConfig, SecurityHeadersFilter}
import play.filters.headers.SecurityHeadersFilter._
import uk.gov.hmrc.play.audit.filters.FrontendAuditFilter
import uk.gov.hmrc.play.audit.http.config.ErrorAuditingSettings
import uk.gov.hmrc.play.filters.frontend.{CSRFExceptionsFilter, HeadersFilter}
import uk.gov.hmrc.play.filters.{CacheControlFilter, RecoveryFilter}
import uk.gov.hmrc.play.frontend.bootstrap.Routing.RemovingOfTrailingSlashes
import uk.gov.hmrc.play.frontend.filters.{SecurityHeadersFilterFactory, DeviceIdCookieFilter, SessionCookieCryptoFilter}
import uk.gov.hmrc.play.graphite.GraphiteConfig
import uk.gov.hmrc.play.http.logging.filters.FrontendLoggingFilter
import uk.gov.hmrc.play.filters.frontend.DeviceIdFilter

trait FrontendFilters {

  def loggingFilter: FrontendLoggingFilter

  def securityFilter: SecurityHeadersFilter

  def frontendAuditFilter: FrontendAuditFilter

  def metricsFilter: MetricsFilter = MetricsFilter

  def deviceIdFilter : DeviceIdFilter

  protected lazy val defaultFrontendFilters: Seq[EssentialFilter] = Seq(
    metricsFilter,
    HeadersFilter,
    SessionCookieCryptoFilter,
    deviceIdFilter,
    loggingFilter,
    frontendAuditFilter,
    CSRFExceptionsFilter,
    CSRFFilter(),
    CacheControlFilter.fromConfig("caching.allowedContentTypes"),
    RecoveryFilter)

  def frontendFilters: Seq[EssentialFilter] = defaultFrontendFilters

}

abstract class DefaultFrontendGlobal
  extends GlobalSettings
  with FrontendFilters
  with GraphiteConfig
  with RemovingOfTrailingSlashes
  with Routing.BlockingOfPaths
  with ErrorAuditingSettings
  with ShowErrorPage {

  lazy val appName = Play.current.configuration.getString("appName").getOrElse("APP NAME NOT SET")
  lazy val enableSecurityHeaderFilter = Play.current.configuration.getBoolean("security.headers.filter.enabled").getOrElse(false)


  override lazy val deviceIdFilter = DeviceIdCookieFilter(appName, auditConnector)

  override def onStart(app: Application) {
    Logger.info(s"Starting frontend : $appName : in mode : ${app.mode}")
    super.onStart(app)
  }

  def filters = if (enableSecurityHeaderFilter) frontendFilters ++ Seq(securityFilter) else frontendFilters

  override def doFilter(a: EssentialAction): EssentialAction =
    Filters(super.doFilter(a), filters: _* )

  override def securityFilter: SecurityHeadersFilter = SecurityHeadersFilterFactory.newInstance

}
