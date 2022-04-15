import org.apache.kafka.clients.producer.ProducerRecord
import zio.*
import zio.kafka.consumer.*
import zio.kafka.producer.*
import zio.kafka.serde.*

object ZioConsumer extends ZIOAppDefault:

  val consumerSettings: ConsumerSettings =
    ConsumerSettings(List("localhost:9092"))
      .withGroupId("zio-group")

  val consumer: ZLayer[Clock, Throwable, Consumer] =
    ZLayer.scoped(Consumer.make(consumerSettings))

  val subscription: Subscription =
    Subscription.topics("demo-topic")

  override def run: ZIO[ZEnv, Throwable, Unit] =
    Consumer
      .subscribeAnd(subscription)
      .plainStream(Serde.string, Serde.string)
      .tap { record =>
        Console.printLine(s"""
             |Received new metadata:
             |Topic     : ${record.record.topic}
             |Partition : ${record.record.partition}
             |Offset    : ${record.record.offset}
             |Timestamp : ${record.record.timestamp}
             |""".stripMargin)
      }
      .map(_.offset)
      .aggregateAsync(Consumer.offsetBatches)
      .mapZIO(_.commit)
      .runDrain
      .provideCustomLayer(consumer)
