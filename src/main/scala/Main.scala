import java.util.concurrent.Executors

import com.kkbox.OAuth
import utils.Http

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Main extends App {
  implicit val ec = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
  private var job: Job = _
  new OAuth("client_key", "client_secret").getAccessTokenByClientCredentials.flatMap {
    accessToken =>
      job = new Job(accessToken)
      job.fetchArtistData
  } flatMap (_ => job.fetchAlbumData) flatMap (_ => job.fetchTrackData) andThen {
    case _ => Http.terminate()
  } onComplete {
    case Success(_) =>
    case Failure(t) => t.printStackTrace()
  }
}
