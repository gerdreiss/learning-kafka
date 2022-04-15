import cats.effect.*
import cats.effect.implicits.*
import cats.implicits.*
import fs2.kafka.*

object Fs2ProducerResource extends IOApp.Simple:

  val settings: ProducerSettings[IO, String, String] =
    ProducerSettings[IO, String, String]
      .withBootstrapServers("localhost:9092")

  def record(id: Any): ProducerRecord[String, String] =
    ProducerRecord("demo-topic", s"Key_$id", s"hello, fs2-kafka $id!")

  def records(num: Int): ProducerRecords[String, String] =
    (1 to num).map(id => ProducerRecords.one(record(id))).reduce(_ ++ _)

  override def run: IO[Unit] =
    KafkaProducer.resource(settings).use {
      _.produce(records(10)).flatTap {
        _.map {
          _.toList
            .map { (_, metadata) =>
              s"""Received new metadata:
               |Topic     : ${metadata.topic}
               |Partition : ${metadata.partition}
               |Offset    : ${metadata.offset}
               |Timestamp : ${metadata.timestamp}
               |""".stripMargin
            }
            .mkString("\n")
        } >>= IO.println
      }.void
    }
