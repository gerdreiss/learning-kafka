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

object ConsumerDemo extends App {

  val logger = LoggerFactory.getLogger(this.getClass())
  val bootstrapServer = "127.0.0.1:9092"
  val stringDeserializer = classOf[StringDeserializer].getName()
  val groupId = "my-first-application"
  val topic = "first_topic"

  // 1. create consumer config
  val properties =
    Map(
      BOOTSTRAP_SERVERS_CONFIG -> bootstrapServer,
      KEY_DESERIALIZER_CLASS_CONFIG -> stringDeserializer,
      VALUE_DESERIALIZER_CLASS_CONFIG -> stringDeserializer,
      GROUP_ID_CONFIG -> groupId,
      AUTO_OFFSET_RESET_CONFIG -> "earliest"
    ).toJavaProperties

  // 2. create consumer
  val consumer = new KafkaConsumer[String, String](properties)

  // 3. subscribe consumer to our topic
  consumer.subscribe(ju.Collections.singleton(topic))

  // poll for new data
  val records = consumer.poll(Duration.ofMillis(100))
  records.forEach { r =>
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
