import org.apache.kafka.clients.producer.ProducerRecord
import sttp.capabilities.zio.ZioStreams
import sttp.client3.*
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import zio.*
import zio.kafka.producer.*
import zio.kafka.serde.*
import zio.stream.*

import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters.*

// FIXME: does not work yet - something's missing...

object WikimediaChangesProducer extends ZIOAppDefault:

  // the topic name
  val topic      = "wikipedia-recent-changes"
  // the record key prefix
  val keyprefix  = "zio-producer-key-"
  // number of configured partitions for the topic
  val partitions = 3

  val request =
    basicRequest
      .get(uri"https://stream.wikimedia.org/v2/stream/recentchange")
      .response(asStreamUnsafe(ZioStreams))

  val producerLayer: TaskLayer[Producer] =
    ZLayer.scoped(Producer.make(ProducerSettings(List("localhost:9092"))))

  val streamRecentChanges =
    AsyncHttpClientZioBackend().flatMap {
      _.send(request)
        .map(_.body)
        .map {
          case Left(error) =>
            ZStream.fail(error)

          case Right(stream) =>
            stream.chunks
              .map(chunk =>
                new String(chunk.toArray).linesIterator
                  .filter(_.startsWith("data: "))
                  .map(_.drop(6))
                  .map(ProducerRecord(topic, keyprefix + util.Random.nextInt(partitions), _))
                  .toArray
              )
              .map(Chunk.apply)
        }
    }

  override def run =
    streamRecentChanges
      .map(_.mapConcatZIO(chunk => Producer.produceChunk(chunk, Serde.string, Serde.string)))
      .provideLayer(AsyncHttpClientZioBackend.layer() ++ producerLayer)
