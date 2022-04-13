import cats.effect.*
import fs2.kafka.*

object UsingFs2Kafka extends IOApp.Simple:

  val settings: ProducerSettings[IO, String, String] =
    ProducerSettings[IO, String, String]
      .withBootstrapServers("localhost:9092")

  val record: ProducerRecord[String, String] =
    ProducerRecord("demo-topic", "fs2", "hello, fs2-kafka!")

  override def run: IO[Unit] =
    KafkaProducer.resource(settings).use(_.produceOne(record).void)
