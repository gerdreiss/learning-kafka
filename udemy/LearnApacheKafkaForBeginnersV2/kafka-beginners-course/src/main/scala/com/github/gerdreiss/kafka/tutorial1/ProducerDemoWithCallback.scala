package com.github.gerdreiss
package kafka
package tutorial1

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.serialization.StringSerializer

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import utils.Extensions._

import scala.util.Properties

import java.{ util => ju }

object ProducerDemoWithCallback extends App {
  val logger = LoggerFactory.getLogger(ProducerDemoWithCallback.getClass())

  val bootstrapServers = "127.0.0.1:9092"
  val stringSerializer = classOf[StringSerializer].getName()

  // 1. create producer properties
  val properties: ju.Properties =
    Map(
      BOOTSTRAP_SERVERS_CONFIG -> bootstrapServers,
      KEY_SERIALIZER_CLASS_CONFIG -> stringSerializer,
      VALUE_SERIALIZER_CLASS_CONFIG -> stringSerializer
    ).toJavaProperties

  // 2. create producer
  val producer: KafkaProducer[String, String] =
    new KafkaProducer[String, String](properties)

  // 3. create a producer record
  val record = new ProducerRecord[String, String]("first_topic", "hello world")

  // 4. send data - asynchronous
  producer.send(
    record,
    new Callback {
      override def onCompletion(
          metadata: RecordMetadata,
          ex: Exception
        ): Unit =
        // executes every time a record is successfully sent
        if (ex == null) {
          logger.info(s"""
          |======================================
          |Received new metadata:
          |======================================
          |  Topic:     ${metadata.topic()}
          |  Partition: ${metadata.partition()}
          |  Offsets:   ${metadata.offset()}
          |  Timestamp: ${metadata.timestamp()}
          |======================================
          """.stripMargin)
        }
        // or an exception is thrown
        else {
          logger.error("", ex)
        }
    }
  )

  // 5. flush data
  producer.flush()

  // 6. close producer
  producer.close()
}
