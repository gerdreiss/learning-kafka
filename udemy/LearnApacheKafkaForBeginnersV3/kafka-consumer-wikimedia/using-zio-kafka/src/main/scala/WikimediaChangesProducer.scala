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

  val request: Request[Either[String, ZioStreams.BinaryStream], ZioStreams] =
    basicRequest
      .get(uri"https://stream.wikimedia.org/v2/stream/recentchange")
      .response(asStreamUnsafe(ZioStreams))

  def mkProducerRecords(chunk: Chunk[Byte]): Chunk[ProducerRecord[String, String]] =
    Chunk.fromIterable(
      new String(chunk.toArray).linesIterator
        .filter(_.startsWith("data: "))
        .map(_.drop(6))
        .map(ProducerRecord(topic, mkKey, _))
        .toArray
    )

  def mkKey = keyprefix + util.Random.nextInt(partitions)

  val recentChanges: Task[Stream[Throwable, Chunk[ProducerRecord[String, String]]]] =
    ArmeriaZioBackend.usingDefaultClient().flatMap {
      _.send(request)
        .map(_.body)
        .map {
          case Left(error) =>
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

  override def run =
    program
      .provideLayer(
        ArmeriaZioBackend.layer() ++ ZLayer.scoped(
          Producer.make(ProducerSettings(List("localhost:9092")))
        )
      )
