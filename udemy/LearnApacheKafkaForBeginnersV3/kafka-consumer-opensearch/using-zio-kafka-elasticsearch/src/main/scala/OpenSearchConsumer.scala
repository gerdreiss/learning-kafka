import zio.*

object OpenSearchConsumer extends ZIOAppDefault:
  def run: ZIO[Environment & ZIOAppArgs & Scope, Any, Any] =
    Console.printLine("Hello ZIO Kafka!")
