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
import org.mockito.ArgumentCaptor
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}
import play.api.http.HeaderNames
import play.api.mvc.{Cookie, Cookies, RequestHeader, Result, Results}
import play.api.test.{FakeApplication, FakeRequest, WithApplication}
import play.filters.headers.SecurityHeadersFilter._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.filters.frontend.{DeviceId, DeviceIdCookie}

import scala.concurrent.Future

class SecurityHeaderFilterSpec extends WordSpecLike with Matchers with MockitoSugar with ScalaFutures {


  def appConfig(decodingEnabled:Boolean) = {
    if (decodingEnabled)
      Map("security.headers.filter.decoding.enabled" -> decodingEnabled, "play.filters.headers.contentSecurityPolicy" -> "ZGVmYXVsdC1zcmMgJ3NlbGYn")
    else Map("security.headers.filter.decoding.enabled" -> false)
  }

  val DECODING_ENABLED = true
  val DECODING_NOT_ENABLED = false

  val auditConnector = mock[AuditConnector]

  trait Setup {
    val action = {
      val mockAction = mock[(RequestHeader) => Future[Result]]
      val outgoingResponse = Future.successful(Results.Ok)
      when(mockAction.apply(any())).thenReturn(outgoingResponse)
      mockAction
    }


  }

  "SecurityHeaderFilter" should {

    "add security header to an http response with filter enabled and  settings decoding disabled" in new WithApplication(FakeApplication(additionalConfiguration = appConfig(DECODING_NOT_ENABLED))) with Setup {

      val incomingRequest = FakeRequest()
      val response = SecurityHeadersFilterFactory.newInstance(action)(incomingRequest).futureValue

      response.header.headers contains("Content-Security-Policy") shouldBe true
      response.header.headers contains("X-Content-Type-Options") shouldBe true
      response.header.headers contains("X-Frame-Options") shouldBe true
      response.header.headers contains("X-Permitted-Cross-Domain-Policies") shouldBe true
      response.header.headers contains("X-XSS-Protection") shouldBe true

      response.header.headers("Content-Security-Policy") shouldBe DEFAULT_CONTENT_SECURITY_POLICY
      response.header.headers("X-Content-Type-Options") shouldBe DEFAULT_CONTENT_TYPE_OPTIONS
      response.header.headers("X-Frame-Options") shouldBe DEFAULT_FRAME_OPTIONS
      response.header.headers("X-Permitted-Cross-Domain-Policies") shouldBe DEFAULT_PERMITTED_CROSS_DOMAIN_POLICIES
      response.header.headers("X-XSS-Protection") shouldBe DEFAULT_XSS_PROTECTION

    }

    "add security header to an http response with filter enabled and  settings decoding enabled" in new WithApplication(FakeApplication(additionalConfiguration = appConfig(DECODING_ENABLED))) with Setup {

      val incomingRequest = FakeRequest()
      val response = SecurityHeadersFilterFactory.newInstance(action)(incomingRequest).futureValue

      response.header.headers contains("Content-Security-Policy") shouldBe true
      response.header.headers contains("X-Content-Type-Options") shouldBe true
      response.header.headers contains("X-Frame-Options") shouldBe true
      response.header.headers contains("X-Permitted-Cross-Domain-Policies") shouldBe true
      response.header.headers contains("X-XSS-Protection") shouldBe true

      response.header.headers("Content-Security-Policy") shouldBe "default-src 'self'"
      response.header.headers("X-Content-Type-Options") shouldBe DEFAULT_CONTENT_TYPE_OPTIONS
      response.header.headers("X-Frame-Options") shouldBe DEFAULT_FRAME_OPTIONS
      response.header.headers("X-Permitted-Cross-Domain-Policies") shouldBe DEFAULT_PERMITTED_CROSS_DOMAIN_POLICIES
      response.header.headers("X-XSS-Protection") shouldBe DEFAULT_XSS_PROTECTION

    }


  }

}
