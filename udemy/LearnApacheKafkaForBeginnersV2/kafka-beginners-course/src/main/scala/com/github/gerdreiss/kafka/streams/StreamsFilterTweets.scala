package com.github.gerdreiss
package kafka
package streams

import com.google.gson.JsonParser
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig._
import org.apache.kafka.streams.kstream.Predicate

import scala.util.Try

import utils.Extensions._

object StreamsFilterTweets {
  // 1. create properties
  val properties = Map(
    BOOTSTRAP_SERVERS_CONFIG         -> "127.0.0.1:9092",
    APPLICATION_ID_CONFIG            -> "demo-kafka-streams",
    DEFAULT_KEY_SERDE_CLASS_CONFIG   -> classOf[Serdes.StringSerde].getName(),
    DEFAULT_VALUE_SERDE_CLASS_CONFIG -> classOf[Serdes.StringSerde].getName()
  ).toJavaProperties

  // 2. create a topology
  val streamsBuilder = new StreamsBuilder
  val inputTopic     = streamsBuilder.stream("twitter_topics")
  val filteredStream = inputTopic.filter(new Predicate[String, String] {
    override def test(key: String, json: String): Boolean =
      (for {
        element <- Try(JsonParser.parseString(json)).toOption
        user    <- Option(element.getAsJsonObject().get("user"))
        count   <- Option(user.getAsJsonObject().get("followers_count"))
      } yield count.getAsInt())
        .exists(_ > 10000)
  })
  filteredStream.to("important_tweets")

  // 3. build the topology
  val kafkaStreams = new KafkaStreams(streamsBuilder.build(), properties)

  // 4. start out streams application
  kafkaStreams.start()
}
