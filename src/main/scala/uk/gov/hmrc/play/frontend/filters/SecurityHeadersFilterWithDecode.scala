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

package uk.gov.hmrc.play.frontend.filters

import org.apache.commons.codec.binary.Base64
import play.api.Play
import play.filters.headers.{SecurityHeadersFilter, DefaultSecurityHeadersConfig}
import play.filters.headers.SecurityHeadersFilter._

object SecurityHeadersFilterFactory {


  val FRAME_OPTIONS_CONFIG_PATH: String = "play.filters.headers.frameOptions"
  val XSS_PROTECTION_CONFIG_PATH: String = "play.filters.headers.xssProtection"
  val CONTENT_TYPE_OPTIONS_CONFIG_PATH: String = "play.filters.headers.contentTypeOptions"
  val PERMITTED_CROSS_DOMAIN_POLICIES_CONFIG_PATH: String = "play.filters.headers.permittedCrossDomainPolicies"
  val CONTENT_SECURITY_POLICY_CONFIG_PATH: String = "play.filters.headers.contentSecurityPolicy"

  lazy val enableSecurityHeaderFilterDecode = Play.current.configuration.getBoolean("security.headers.filter.decoding.enabled").getOrElse(false)

  def readAndDecodeConfigValue(configPropertyName: String, defaultPropertyValue: String) = Play.current.configuration.getString(configPropertyName)
    .fold(defaultPropertyValue) { value =>
    if (enableSecurityHeaderFilterDecode)
       new String(Base64.decodeBase64(value))

    else value
  }


  val frameOptions: String = readAndDecodeConfigValue(FRAME_OPTIONS_CONFIG_PATH, DEFAULT_FRAME_OPTIONS)
  val xssProtection: String = readAndDecodeConfigValue(XSS_PROTECTION_CONFIG_PATH, DEFAULT_XSS_PROTECTION)
  val contentTypeOptions: String = readAndDecodeConfigValue(CONTENT_TYPE_OPTIONS_CONFIG_PATH, DEFAULT_CONTENT_TYPE_OPTIONS)
  val permittedCrossDomainPolicies: String = readAndDecodeConfigValue(PERMITTED_CROSS_DOMAIN_POLICIES_CONFIG_PATH, DEFAULT_PERMITTED_CROSS_DOMAIN_POLICIES)
  val contentSecurityPolicy: String = readAndDecodeConfigValue(CONTENT_SECURITY_POLICY_CONFIG_PATH, DEFAULT_CONTENT_SECURITY_POLICY)

  val config = DefaultSecurityHeadersConfig(
    frameOptions = Option(frameOptions),
    xssProtection = Option(xssProtection),
    contentTypeOptions = Option(contentTypeOptions),
    permittedCrossDomainPolicies = Option(permittedCrossDomainPolicies),
    contentSecurityPolicy = Option(contentSecurityPolicy))


  def newInstance = SecurityHeadersFilter(config)

}
