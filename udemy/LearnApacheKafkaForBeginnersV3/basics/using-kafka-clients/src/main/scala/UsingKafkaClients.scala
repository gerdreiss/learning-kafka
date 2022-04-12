import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory

import scala.collection.immutable.{HashMap, StringOps}
import scala.jdk.CollectionConverters.*

object UsingKafkaClients extends App:

  val logger = LoggerFactory.getLogger(getClass).nn
  
  logger.info("Starting UsingKafkaClients")

  // create Producer Properties
  val properties = Map(
    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG      -> "127.0.0.1:9092",
    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG   -> classOf[StringSerializer].getName,
    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> classOf[StringSerializer].getName
  )

  // create the Producer
  val producer = new KafkaProducer[String, String](properties.asJava)

  // create a Producer Record
  val record = new ProducerRecord[String, String]("demo-topic", "hello, kafka-clients!")

  // send the record - asynchronous
  producer.send(record)

  // flush and close the producer - synchronous
  producer.flush()
  producer.close()
