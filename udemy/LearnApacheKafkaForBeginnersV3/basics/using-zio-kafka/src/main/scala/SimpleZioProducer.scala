import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerRecord }
import zio.*
import zio.kafka.producer.*
import zio.kafka.serde.*

object SimpleZioProducer extends ZIOAppDefault:

  val producer: ZIO[Scope, Throwable, Producer] =
    Producer.make(ProducerSettings(List("localhost:9092")))

  val record: ProducerRecord[String, String] =
    ProducerRecord("demo-topic", "zio", "hello, zio-kafka")

  override def run: Task[Any] =
    Producer
      .produce(record, Serde.string, Serde.string)
      .provideLayer(ZLayer.scoped(producer))
