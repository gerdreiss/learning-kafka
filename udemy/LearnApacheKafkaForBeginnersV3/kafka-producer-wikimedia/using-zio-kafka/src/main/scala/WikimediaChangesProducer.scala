import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import sttp.capabilities.zio.ZioStreams
import sttp.client3.*
import sttp.client3.armeria.zio.ArmeriaZioBackend
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

  def mkKey = keyprefix + util.Random.nextInt(partitions)

  def mkProducerRecords(chunk: Chunk[Byte]): Chunk[ProducerRecord[String, String]] =
    Chunk.fromIterable(
      new String(chunk.toArray).linesIterator
        .filter(_.startsWith("data: "))
        .map(chunk => ProducerRecord(topic, mkKey, chunk.drop(6)))
        .toArray
    )

  val request: Request[Either[String, ZioStreams.BinaryStream], ZioStreams] =
    basicRequest
      .get(uri"https://stream.wikimedia.org/v2/stream/recentchange")
      .response(asStreamUnsafe(ZioStreams))

  val recentChanges: Task[Stream[Throwable, Chunk[ProducerRecord[String, String]]]] =
    ArmeriaZioBackend.usingDefaultClient().flatMap {
      _.send(request)
        .map(_.body)
        .map {
          case Left(error)   =>
            ZStream.fail(new Exception(error))
          case Right(stream) =>
            stream.chunks.map(mkProducerRecords)
        }
    }

  val program: ZIO[Producer, Throwable, Unit] =
    for
      changes <- recentChanges
      _       <- changes
                   .map(chunk => Producer.produceChunk(chunk, Serde.string, Serde.string))
                   .runDrain
    yield ()

  val armeriaLayer  = ArmeriaZioBackend.layer()
  val producerLayer =
    ZLayer.scoped(
      Producer.make(
        ProducerSettings(List("localhost:9092"))
          .withProperties(
            Map(
              // safe producer config for Kafka <= 2.8
              ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG             -> "true",
              ProducerConfig.ACKS_CONFIG                           -> "all",
              ProducerConfig.RETRIES_CONFIG                        -> Integer.MAX_VALUE.toString,
              ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION -> "5",
              ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG            -> "120000",
              // high throughput producer (at the expence of a bit of latency and CPU usage)
              ProducerConfig.COMPRESSION_TYPE_CONFIG               -> "snappy",
              ProducerConfig.LINGER_MS_CONFIG                      -> "20",
              ProducerConfig.BATCH_SIZE_CONFIG                     -> Integer.toString(32 * 1024)
            )
          )
      )
    )

  override def run =
    program.exitCode.provideLayer(armeriaLayer ++ producerLayer)
