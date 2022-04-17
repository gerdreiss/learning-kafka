import sttp.capabilities.zio.ZioStreams
import sttp.client3.*
import sttp.client3.httpclient.zio.*
import zio.*
import zio.stream.*
//import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerRecord }
// import zio.kafka.producer.*
// import zio.kafka.serde.*

import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters.*

// FIXME: That stuff doesn't even compile when including zio-kafka dependency:
//  java.lang.RuntimeException: Conflicting cross-version suffixes in: org.scala-lang.modules:scala-collection-compat
object WikimediaChangesProducer extends ZIOAppDefault:

  // number of configured partitions for the topic
  val partitions      = 3
  // the group size for the records to be produced before sending them to Kafka
  val recordGroupSize = 10
  // the stream URL
  val recentchangeUrl = uri"https://stream.wikimedia.org/v2/stream/recentchange"
  // the topic name
  val topic           = "wikipedia-recent-changes"
  // the record key prefix
  val keyprefix       = "zio-producer-key-"

  val request =
    basicRequest
      .get(uri"https://stream.wikimedia.org/v2/stream/recentchange")
      .response(asStreamUnsafe(ZioStreams))

  val response = send(request)
    .map(_.body)
    .tap {
      case Left(error)   =>
        Console.printLine(s"Error: $error")
      case Right(stream) =>
        stream.chunks
          .map(chunk =>
            new String(chunk.toArray).lines.toList.asScala
              .find(_.startsWith("data: "))
              .map(_.drop(6))
            // .map(ProducerRecord(topic, keyprefix + Random.nextInt(partitions), _))
          )
          .take(1)
          .map(println)
          .runDrain
    }

  override def run =
    response.provide(HttpClientZioBackend.layer())
