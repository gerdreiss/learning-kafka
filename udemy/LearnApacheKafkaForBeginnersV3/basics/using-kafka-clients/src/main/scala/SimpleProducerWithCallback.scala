import org.apache.kafka.clients.producer.*
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters.*

object SimpleProducerWithCallback extends App:

  val logger = LoggerFactory.getLogger(getClass).nn

  logger.info("Simple Producer With Callback")

  val properties = Map(
    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG      -> "127.0.0.1:9092",
    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG   -> classOf[StringSerializer].getName,
    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> classOf[StringSerializer].getName
  )

  // create the Producer
  val producer = new KafkaProducer[String, String](properties.asJava)

  // create a Producer Record
  val record =
    new ProducerRecord[String, String]("demo-topic", "hello, kafka-clients with callback!")

  val callback: Callback = new Callback:
    override def onCompletion(metadata: RecordMetadata, error: Exception | Null): Unit =
      // executes every time a record is successfully sent or an exception is thrown
      if error == null then logger.info(s"""Received new metadata:
                                           |Topic: ${metadata.topic}
                                           |Partition: ${metadata.partition}
                                           |Offset: ${metadata.offset}
                                           |Timestamp: ${metadata.timestamp}
                                           |""".stripMargin)
      else logger.error(s"Something bad happened: ${error.getMessage}")

  // send the record - asynchronous
  producer.send(record, callback)

  // flush and close the producer - synchronous
  producer.flush()
  producer.close()
