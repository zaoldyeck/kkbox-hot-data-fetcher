package com.kkbox

import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.JsValue
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.StandaloneWSRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class API(accessToken: String) {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  private val domain = "https://api.kkbox.com/v1.1"
  private val client = new Client(accessToken)

  def getChartIds(territory: String = "TW"): Future[List[String]] = {
    client.url(s"$domain/charts?territory=$territory").get().map {
      response =>
        (response.body[JsValue] \ "data").as[List[JsValue]].map(_.apply("id").as[String])
    } recoverWith {
      case e: Exception =>
        e.printStackTrace()
        getChartIds(territory)
    }
  }

  def getArtistsByPlaylistId(playlistId: String, territory: String = "TW"): Future[List[Artist]] = {
    client.url(s"$domain/shared-playlists/$playlistId/tracks?territory=$territory").get().map {
      response =>
        (response.body[JsValue] \ "data").as[List[JsValue]].map {
          track =>
            val artist = track \ "album" \ "artist"
            val artistId = (artist \ "id").as[String]
            val artistName = (artist \ "name").as[String]
            Artist(artistId, artistName)
        }
    } recoverWith {
      case e: Exception =>
        e.printStackTrace()
        getArtistsByPlaylistId(playlistId, territory)
    }
  }

  def getAlbumsByArtistId(artistId: String, territory: String = "TW"): Future[List[Album]] = {
    client.url(s"$domain/artists/$artistId/albums?territory=$territory").get().map {
      response =>
        (response.body[JsValue] \ "data").asOpt[List[JsValue]].map(_.map {
          album =>
            val albumId = (album \ "id").as[String]
            val albumName = (album \ "name").as[String]
            Album(albumId, albumName)
        }) getOrElse List()
    } recoverWith {
      case e: Exception =>
        e.printStackTrace()
        getAlbumsByArtistId(artistId, territory)
    }
  }

  def getTracksByAlbumId(albumId: String, territory: String = "TW"): Future[List[Track]] = {
    client.url(s"$domain/albums/$albumId/tracks?territory=$territory").get().map {
      response =>
        (response.body[JsValue] \ "data").as[List[JsValue]].map {
          track =>
            val trackId = (track \ "id").as[String]
            val trackName = (track \ "name").as[String]
            Track(trackId, trackName)
        }
    } recoverWith {
      case e: Exception =>
        e.printStackTrace()
        getTracksByAlbumId(albumId, territory)
    }
  }

  def getTrackDataByTrackId(trackId: String, territory: String = "TW"): Future[Option[TrackData]] = {
    //Thread.sleep(5000)
    client.url(s"$domain/tracks/$trackId?territory=$territory").get().map {
      response =>
        val track = response.body[JsValue]
        val trackIdOption = (track \ "id").asOpt[String]
        trackIdOption match {
          case Some(id) =>
            val trackName = (track \ "name").as[String]
            val album = track \ "album"
            val albumId = (album \ "id").asOpt[String].getOrElse("")
            val albumName = (album \ "name").asOpt[String].getOrElse("")
            val artist = track \ "artist"
            val artistId = (artist \ "id").asOpt[String].getOrElse("")
            val artistName = (artist \ "name").asOpt[String].getOrElse("")
            Some(TrackData(id, trackName, albumId, albumName, artistId, artistName))
          case None => None
        }
    } recoverWith {
      case e: Exception =>
        e.printStackTrace()
        getTrackDataByTrackId(trackId, territory)
    }
  }

  case class Artist(id: String, name: String)

  case class Album(id: String, name: String)

  case class Track(id: String, name: String)

  case class TrackData(id: String, name: String, albumId: String, albumName: String, artistId: String, artistName: String) {
    override def toString: String = s"$id,$name,$albumId,$albumName,$artistId,$artistName"
  }

}