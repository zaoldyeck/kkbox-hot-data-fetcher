package com.kkbox

import play.api.libs.ws.StandaloneWSRequest
import utils.Http

import scala.concurrent.Future

class Client(accessToken: String) {
  private val header = "Authorization" -> s"Bearer $accessToken"
  private var url = ""

  def url(url: String): Client = {
    this.url = url
    this
  }

  def get(): Future[StandaloneWSRequest#Response] = {
    Http.client.url(url).addHttpHeaders(header).get()
  }

  def post() = {

  }
}
