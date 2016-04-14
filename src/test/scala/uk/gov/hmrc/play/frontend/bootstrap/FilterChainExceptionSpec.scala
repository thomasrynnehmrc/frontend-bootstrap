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

import org.scalatest.{Matchers, WordSpecLike}
import play.api.libs.concurrent.Execution.{defaultContext => ec}
import play.api.mvc._
import play.api.test.{FakeApplication, WithServer, WsTestClient}
import play.filters.headers.SecurityHeadersFilter
import uk.gov.hmrc.play.filters.RecoveryFilter
import uk.gov.hmrc.play.http.NotFoundException

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class FilterChainExceptionSpec extends WordSpecLike with Matchers with WsTestClient {

  val routerForTest: PartialFunction[(String, String), Handler] = {
    case ("GET", "/ok") => Action { request => Results.Ok("OK") }
    case ("GET", "/error-async-404") => Action.async { request => Future { throw new NotFoundException("Expect 404") }(ec) }
  }

  object FiltersForTestWithSecurityFilterFirst extends WithFilters(SecurityHeadersFilter(), RecoveryFilter)
  object FiltersForTestWithSecurityFilterLast extends WithFilters(RecoveryFilter, SecurityHeadersFilter())

  "Action throws no exception and returns 200 OK" in new  WithServer(FakeApplication(withGlobal = Some(FiltersForTestWithSecurityFilterFirst), withRoutes = routerForTest))  {
    val response = Await.result(wsUrl("/ok").get(), Duration.Inf)
    response.status shouldBe (200)
    response.body shouldBe ("OK")
  }

  "Action throws NotFoundException and returns 404" in new  WithServer(FakeApplication(withGlobal = Some(FiltersForTestWithSecurityFilterFirst), withRoutes = routerForTest))  {
    val response = Await.result(wsUrl("/error-async-404").get(), Duration.Inf)
    response.status shouldBe (404)
    response.body shouldBe ("Expect 404")
  }

  "Action throws NotFoundException, but filters throw an InternalServerError" in new  WithServer(FakeApplication(withGlobal = Some(FiltersForTestWithSecurityFilterLast), withRoutes = routerForTest))  {
    val response = Await.result(wsUrl("/error-async-404").get(), Duration.Inf)
    response.status shouldBe (500)
  }
}
