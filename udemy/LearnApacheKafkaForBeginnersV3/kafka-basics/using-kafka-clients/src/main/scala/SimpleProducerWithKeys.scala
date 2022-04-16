import org.apache.kafka.clients.producer.*
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters.*

object SimpleProducerWithKeys extends App:

  val properties = Map(
    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG      -> "127.0.0.1:9092",
    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG   -> classOf[StringSerializer].getName,
    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> classOf[StringSerializer].getName
  )

  // create the Producer
  val producer = new KafkaProducer[String, String](properties.asJava)

  // create a Producer Record
  def record(id: Any) =
    new ProducerRecord[String, String](
      "demo-topic",
      s"Key_$id",
      s"$id) hello, kafka-clients with callback and key!"
    )

  val callback: Callback = new Callback:
    override def onCompletion(metadata: RecordMetadata, error: Exception | Null): Unit =
      // executes every time a record is successfully sent or an exception is thrown
      if error == null then println(s"""Received new metadata:
                                       |Topic     : ${metadata.topic}
                                       |Partition : ${metadata.partition}
                                       |Offset    : ${metadata.offset}
                                       |Timestamp : ${metadata.timestamp}
                                       |""".stripMargin)
      else println(s"Something bad happened: ${error.getMessage}")

  // send the record - asynchronous
  (1 to 10).foreach(i => producer.send(record(i), callback))

  // flush and close the producer - synchronous
  producer.flush()
  producer.close()
