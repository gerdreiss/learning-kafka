import cats.effect.*

object WikimediaChangesProducer extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    for _ <- IO(println("Hello, FS2 Kafka!"))
    yield ExitCode.Success
