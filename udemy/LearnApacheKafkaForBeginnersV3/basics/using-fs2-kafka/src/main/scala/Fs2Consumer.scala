import cats.effect.*
import cats.syntax.all.*
import fs2.kafka.*
import scala.concurrent.duration.*

object Fs2Consumer extends IOApp.Simple:

  val consumerSettings: ConsumerSettings[IO, String, String] =
    ConsumerSettings[IO, String, String]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("localhost:9092")
      .withGroupId("fs2-group")

  // FIXME: this shit fails with:
  //  java.lang.NullPointerException: Cannot read the array length because "bytes" is null
  override def run: IO[Unit] =
    KafkaConsumer
      .stream(consumerSettings)
      .subscribeTo("demo-topic")
      .records
      .mapAsync(25) { committable =>
        IO(println(s"Processing record: ${committable.record}"))
      }
      .compile
      .drain
