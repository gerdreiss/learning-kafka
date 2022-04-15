import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerRecord }
import zio.*
import zio.kafka.producer.*
import zio.kafka.serde.*

object ZioOneRecordProducer extends ZIOAppDefault:

  val producerLayer: TaskLayer[Producer] =
    ZLayer.scoped(Producer.make(ProducerSettings(List("localhost:9092"))))

  val record: ProducerRecord[String, String] =
    ProducerRecord("demo-topic", "zio", "hello, zio-kafka")

  override def run: Task[Any] =
    Producer
      .produce(record, Serde.string, Serde.string)
      .provideLayer(producerLayer)
