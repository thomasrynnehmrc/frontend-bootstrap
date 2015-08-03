/*
 * Copyright 2015 HM Revenue & Customs
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
import play.api.mvc.Handler
import play.api.test.{FakeApplication, FakeRequest, WithApplication}

import scala.util.matching.Regex

class BlockingOfPathsSpec extends WordSpecLike with Matchers {

  "The onRouteRequest method" should {
    val `blocks /paye/*` = Some( """\/paye\/.*""".r)
    val `allows everything` = None
    val everythingRoutesToAHandler: PartialFunction[(String, String), Handler] = {
      case _ => new Handler {}
    }

    abstract class TestCase(pattern: Option[Regex]) extends WithApplication(FakeApplication(withRoutes = everythingRoutesToAHandler)) {
      lazy val BlockingOfPaths = new Routing.BlockingOfPaths {
        override def blockedPathPattern: Option[Regex] = pattern
      }
    }

    "block the urls that match the regex given as config" in new TestCase(pattern = `blocks /paye/*`) {
      BlockingOfPaths.onRouteRequest(FakeRequest("GET", "/paye/company-car")) should not be defined
    }

    "allow the urls that do not match the regex given as config" in new TestCase(pattern = `blocks /paye/*`) {
      BlockingOfPaths.onRouteRequest(FakeRequest("GET", "/aye/company-car")) should be(defined)
    }

    "allow any url if no regex is given in config" in new TestCase(pattern = `allows everything`) {
      BlockingOfPaths.onRouteRequest(FakeRequest("GET", "/paye/company-car")) should be(defined)
    }
  }
}
