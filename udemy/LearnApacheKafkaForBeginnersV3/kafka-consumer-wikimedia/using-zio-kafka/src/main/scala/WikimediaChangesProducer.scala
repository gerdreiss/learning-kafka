import zio.*

object WikimediaChangesProducer extends ZIOAppDefault:

  def run: ZIO[Environment, Throwable, Unit] =
    Console.printLine("Hello, ZIO Kafka!")
