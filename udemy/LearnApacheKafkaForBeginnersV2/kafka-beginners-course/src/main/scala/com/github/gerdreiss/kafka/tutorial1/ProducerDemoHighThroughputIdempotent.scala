package com.github.gerdreiss
package kafka
package tutorial1

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig._
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import utils.Extensions._

import scala.util.Properties

import java.{ util => ju }

object ProducerDemoHighThroughputIdempotent extends App {
  val bootstrapServers = "127.0.0.1:9092"
  val stringSerializer = classOf[StringSerializer].getName()

  // 1. create producer properties
  val properties: ju.Properties =
    Map(
      BOOTSTRAP_SERVERS_CONFIG              -> bootstrapServers,
      KEY_SERIALIZER_CLASS_CONFIG           -> stringSerializer,
      VALUE_SERIALIZER_CLASS_CONFIG         -> stringSerializer,
      // safe producer
      ENABLE_IDEMPOTENCE_CONFIG             -> "true",
      ACKS_CONFIG                           -> "all",
      RETRIES_CONFIG                        -> Integer.MAX_VALUE.toString(),
      MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION -> "5",
      // high throughput producer at the expense of a bit of latency and CPU usage
      COMPRESSION_TYPE_CONFIG               -> "snappy",
      LINGER_MS_CONFIG                      -> "20",
      BATCH_SIZE_CONFIG                     -> Integer.toString(32 * 1024) // 32KB
    ).toJavaProperties

  // 2. create producer
  val producer: KafkaProducer[String, String] =
    new KafkaProducer[String, String](properties)

  // 3. create a producer record
  val record = new ProducerRecord[String, String]("first_topic", "hello world")

  // 4. send data - asynchronous
  producer.send(record)

  // 5. flush data
  producer.flush()

  // 6. close producer
  producer.close()
}
