import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerRecord }
import zio.*
import zio.kafka.producer.*
import zio.kafka.serde.*

object ZioChunkProducer extends ZIOAppDefault:

  val producerLayer: TaskLayer[Producer] =
    ZLayer.scoped(Producer.make(ProducerSettings(List("localhost:9092"))))

  def record(id: Any): ProducerRecord[String, String] =
    ProducerRecord("demo-topic", s"zio-$id", s"hello, zio-kafka $id!")

  def records(num: Int): Chunk[ProducerRecord[String, String]] =
    Chunk.fromIterable((1 to num).map(record))

  override def run: Task[Any] =
    Producer
      .produceChunk(records(10), Serde.string, Serde.string)
      .tap { chunk =>
        val s = chunk
          .map { metadata =>
            s"""Received new metadata:
             |Topic     : ${metadata.topic}
             |Partition : ${metadata.partition}
             |Offset    : ${metadata.offset}
             |Timestamp : ${metadata.timestamp}
             |""".stripMargin
          }
          .mkString("\n")
        Console.printLine(s)
      }
      .provideLayer(producerLayer ++ Console.live)
