import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp

object OpenSearchConsumer extends IOApp:

  def run(args: List[String]): IO[ExitCode] =
    IO.println("Hello FS2 Kafka!").as(ExitCode.Success)
