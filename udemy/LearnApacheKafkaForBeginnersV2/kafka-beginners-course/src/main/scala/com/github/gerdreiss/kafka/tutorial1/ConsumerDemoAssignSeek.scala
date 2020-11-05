package com.github.gerdreiss
package kafka
package tutorial1

import org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory

import utils.Extensions._

import java.{ util => ju }
import java.time.Duration
import org.apache.kafka.common.TopicPartition

object ConsumerDemoAssignSeek extends App {

  val logger             = LoggerFactory.getLogger(this.getClass())
  val bootstrapServer    = "127.0.0.1:9092"
  val stringDeserializer = classOf[StringDeserializer].getName()
  val topic              = "first_topic"

  // 1. create consumer config
  val properties =
    Map(
      BOOTSTRAP_SERVERS_CONFIG        -> bootstrapServer,
      KEY_DESERIALIZER_CLASS_CONFIG   -> stringDeserializer,
      VALUE_DESERIALIZER_CLASS_CONFIG -> stringDeserializer,
      AUTO_OFFSET_RESET_CONFIG        -> "earliest"
    ).toJavaProperties

  // 2. create consumer
  val consumer = new KafkaConsumer[String, String](properties)

  // 3. assign
  val partitionToReadFrom = new TopicPartition(topic, 0)
  val offsetToReadFrom    = 15L
  consumer.assign(ju.Collections.singleton(partitionToReadFrom))

  // 3.5 seek
  consumer.seek(partitionToReadFrom, offsetToReadFrom)

  // 4. poll for new data
  consumer
    .poll(Duration.ofMillis(100))
    .forEach { r =>
      logger.info(s"""
        |======================================
        |Received new record:
        |======================================
        |  Key: ${r.key()}
        |  Value: ${r.value()}
        |  Partition: ${r.partition()}
        |  Offset: ${r.offset()}
        |======================================
        """.stripMargin)
    }
}
