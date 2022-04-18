import cats.effect.*
import cats.implicits.*
import fs2.*
import fs2.kafka.*
import org.apache.kafka.clients.producer.ProducerConfig
import org.http4s.Request
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.syntax.literals.uri

import scala.jdk.CollectionConverters.*
import scala.util.Random
import scala.concurrent.duration.*

object WikimediaChangesProducer extends IOApp:

  // number of configured partitions for the topic
  val partitions      = 3
  // the group size for the records to be produced before sending them to Kafka
  val recordGroupSize = 10
  // the stream URL
  val recentchangeUrl = uri"https://stream.wikimedia.org/v2/stream/recentchange"
  // the topic name
  val topic           = "wikipedia-recent-changes"
  // the record key prefix
  val keyprefix       = "fs2-producer-key-"

  val producerSettings = ProducerSettings[IO, String, String]
    .withBootstrapServers("localhost:9092")
    // safe producer config for Kafka <= 2.8
    .withAcks(Acks.All)
    .withRetries(Int.MaxValue)
    .withEnableIdempotence(true)
    .withMaxInFlightRequestsPerConnection(5)
    .withDeliveryTimeout(2.minutes)

  val recentChangesStream =
    for
      resource <- Stream.resource(EmberClientBuilder.default[IO].build)
      stream   <- resource.stream(Request[IO](uri = recentchangeUrl))
      records  <- stream.body.chunks
                    .map(chunk => new String(chunk.toArray))
                    .filter(_.startsWith("data: "))
                    .map(_.drop(6))
                    .map(ProducerRecord(topic, keyprefix + Random.nextInt(partitions), _))
                    .sliding(recordGroupSize, recordGroupSize)
                    .map(ProducerRecords.apply)
    yield records

  def run(args: List[String]): IO[ExitCode] =
    recentChangesStream
      .through(KafkaProducer.pipe(producerSettings))
      .compile
      .drain
      .as(ExitCode.Success)
