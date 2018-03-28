package com.kkbox

import org.scalatest.FunSuite
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global

class OAuthTest extends FunSuite {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  private val oAuth = new OAuth("client_key", "client_secret")

  test("getTokenByClientCredentials") {
    oAuth.getAccessTokenByClientCredentials.map {
      accessToken =>
        logger.error(accessToken)
        assert(accessToken.isInstanceOf[String] && accessToken.nonEmpty)
    }
  }
}
