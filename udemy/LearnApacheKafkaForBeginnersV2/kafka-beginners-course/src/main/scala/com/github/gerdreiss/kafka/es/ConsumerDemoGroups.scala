package com.github.gerdreiss
package kafka
package es

import org.apache.kafka.clients.consumer.ConsumerConfig._
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory

import utils.Extensions._

import java.{ util => ju }
import java.time.Duration

object ConsumerDemoGroups {

  val logger             = LoggerFactory.getLogger(this.getClass())
  val bootstrapServer    = "127.0.0.1:9092"
  val stringDeserializer = classOf[StringDeserializer].getName()
  val groupId            = "kafka-demo-elasticsearch"
  val topic              = "twitter_tweets"

  def createConsumer: KafkaConsumer[String, String] = {

    // 1. create consumer config
    val properties =
      Map(
        BOOTSTRAP_SERVERS_CONFIG        -> bootstrapServer,
        KEY_DESERIALIZER_CLASS_CONFIG   -> stringDeserializer,
        VALUE_DESERIALIZER_CLASS_CONFIG -> stringDeserializer,
        GROUP_ID_CONFIG                 -> groupId,
        AUTO_OFFSET_RESET_CONFIG        -> "earliest",
        ENABLE_AUTO_COMMIT_CONFIG       -> "false",
        MAX_POLL_RECORDS_CONFIG         -> "100"
      ).toJavaProperties

    // 2. create consumer
    val consumer = new KafkaConsumer[String, String](properties)

    // 3. subscribe consumer to our topic
    consumer.subscribe(ju.Collections.singleton(topic))

    consumer
  }
}
