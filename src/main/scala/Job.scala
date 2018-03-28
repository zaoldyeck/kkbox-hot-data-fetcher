import java.io.{File, PrintWriter}

import com.kkbox.API

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

class Job(accessToken: String) {
  private val api: API = new API(accessToken)

  def fetchArtistData: Future[Unit] = {
    val artistWriter = new PrintWriter(new File("artist.csv"))
    val artistIdWriter = new PrintWriter(new File("artist_id.csv"))

    api.getChartIds().flatMap {
      chartIds =>
        Future.sequence(chartIds.map {
          chartId =>
            api.getArtistsByPlaylistId(chartId)
        }) map (_.reduce(_ ::: _))
    } map {
      artists =>
        artists.par.foreach {
          artist =>
            checkNameWithAlias(artist.name, artistWriter)
            artistIdWriter.write(artist.id + "\n")
        }
        artistWriter.close()
        artistIdWriter.close()
    }
  }

  def fetchAlbumData: Future[Iterator[Unit]] = {
    val albumWriter = new PrintWriter(new File("album.csv"))
    val albumIdWriter = new PrintWriter(new File("album_id.csv"))

    Future.sequence(Source.fromFile("./artist_id.csv").getLines().map {
      artistId =>
        api.getAlbumsByArtistId(artistId).map {
          albums =>
            albums.foreach {
              album =>
                checkNameWithAlias(album.name, albumWriter)
                albumIdWriter.write(album.id + "\n")
            }
        }
    })
  }

  def fetchTrackData: Future[Iterator[Unit]] = {
    val trackWriter = new PrintWriter(new File("track.csv"))

    Future.sequence(Source.fromFile("./album_id.csv").getLines().map {
      albumId =>
        api.getTracksByAlbumId(albumId).map {
          tracks =>
            tracks.foreach {
              track =>
                checkNameWithAlias(track.name, trackWriter)
            }
        }
    })
  }

  def fetchTopTrackData: Future[Iterator[Unit]] = {
    val trackDataWriter = new PrintWriter(new File("top_track_data.csv"))
    trackDataWriter.write("track_id,track_name,album_id,album_name,artist_id,artist_name" + "\n")

    val filePath = getClass.getResource("kkbox_top_50000_track_id.csv").getPath
    Future.sequence(Source.fromFile(filePath).getLines().map {
      trackId =>
        api.getTrackDataByTrackId(trackId).map {
          trackDataOption =>
            trackDataOption.foreach {
              trackData =>
                trackDataWriter.write(trackData.toString + "\n")
            }
        }
    })
  }

  private def checkNameWithAlias(name: String, writer: PrintWriter): Unit = {
    if (name.nonEmpty) {
      val n = name.replace(",", "").replace("\"", "")
      if (n.endsWith(")")) {
        val strings = n.split("\\(")
        val string0 = strings(0)
        val name1 = if (string0.endsWith(" ")) string0.substring(0, string0.length - 1) else string0
        val name2 = strings(1).replace(")", "")
        writer.write("\"" + name1 + "\",\"" + name1 + "\"\n")
        writer.write("\"" + name2 + "\",\"" + name2 + "\"\n")
      } else {
        writer.write("\"" + n + "\",\"" + n + "\"\n")
      }
    }
  }
}
