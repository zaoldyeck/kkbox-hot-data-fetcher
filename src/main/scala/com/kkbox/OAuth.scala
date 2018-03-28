package com.kkbox

import play.api.libs.json.{JsValue, Json, Reads}
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.WSAuthScheme.BASIC
import utils.Http

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class OAuth(clientKey: String, clientSecret: String) {
  def getAccessTokenByClientCredentials: Future[String] = {
    Http.client.url("https://account.kkbox.com/oauth2/token")
      .withAuth(clientKey, clientSecret, BASIC)
      .post(Map("grant_type" -> "client_credentials")).map {
      response => response.body[JsValue].as[ResAccessToken].access_token
    }
  }

  case class ResAccessToken(access_token: String, expires_in: Int, token_type: String)

  implicit private val resAccessTokenReads: Reads[ResAccessToken] = Json.reads[ResAccessToken]
}
