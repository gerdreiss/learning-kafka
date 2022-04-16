import org.apache.kafka.clients.consumer.{ ConsumerConfig, ConsumerRecords, KafkaConsumer }
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory

import java.time.Duration as JDuration
import scala.concurrent.duration.*
import scala.jdk.CollectionConverters.*

object SimpleConsumer extends App:

  val properties = Map(
    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG        -> "localhost:9092",
    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG   -> classOf[StringDeserializer].getName,
    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer].getName,
    ConsumerConfig.GROUP_ID_CONFIG                 -> "test-consumer-group",
    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG        -> "earliest"
  )

  val consumer = new KafkaConsumer[String, String](properties.asJava)

  consumer.subscribe(List("demo-topic").asJava)

  while true do
    consumer
      .poll(JDuration.ofMillis(100))
      .nn
      .asScala
      .map(record => s"""
           |key: ${record.key()}
           |value: ${record.value()}
           |partition: ${record.partition()}
           |offset: ${record.offset()}
           |""".stripMargin)
      .foreach(println)
