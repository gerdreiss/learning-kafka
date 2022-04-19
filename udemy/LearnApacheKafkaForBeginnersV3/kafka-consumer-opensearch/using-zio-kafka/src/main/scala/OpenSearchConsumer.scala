import zio.ZIOAppDefault

object OpenSearchConsumer extends ZIOAppDefault:
  def run = zio.Console.printLine("Hello ZIO Kafka!")
