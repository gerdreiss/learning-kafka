// import sttp.capabilities.zio.ZioStreams
// import sttp.capabilities.zio.ZioStreams
// import sttp.client3.httpclient.zio.*
// import zio.stream.*
import zio.*

object WikimediaChangesProducer extends ZIOAppDefault:

  // FIXME: That stuff doesn't even compile:
  //  java.lang.RuntimeException: Conflicting cross-version suffixes in: org.scala-lang.modules:scala-collection-compat

  // val request =
  //   basicRequest
  //     .get(uri"https://stream.wikimedia.org/v2/stream/recentchange")
  //     .response(asStreamUnsafe(ZioStreams))
  //     .readTimeout(java.time.Duration.Inf)
  // val response = send(request)
  //   .tapEach(r => println(r.body))

  override def run =
    Console.printLine("Hello Kafka Clients")
    // response.provide(HttpClientZioBackend.layer(), Console.live)
