import sttp.capabilities.zio.ZioStreams
import sttp.client3.*
import sttp.client3.httpclient.zio.HttpClientZioBackend
import sttp.client3.httpclient.zio.SttpClient
import sttp.client3.httpclient.zio.send
import zio.*
import zio.stream.*

import scala.concurrent.duration.Duration

object WikimediaChangesProducer extends ZIOAppDefault:

  // FIXME: That stuff doesn't even compile when including zio-kafka dependency:
  //  java.lang.RuntimeException: Conflicting cross-version suffixes in: org.scala-lang.modules:scala-collection-compat

  val request  =
    basicRequest
      .get(uri"https://stream.wikimedia.org/v2/stream/recentchange")
      .response(asStreamUnsafe(ZioStreams))
  val response = send(request)
    .map(_.body)
    .tap {
      case Left(error)   =>
        Console.printLine(s"Error: $error")
      case Right(stream) =>
        stream.map(println).runDrain
    }

  override def run =
    response.provide(HttpClientZioBackend.layer())
