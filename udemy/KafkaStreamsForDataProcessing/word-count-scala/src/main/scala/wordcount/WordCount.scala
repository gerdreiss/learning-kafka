package wordcount

import java.time.Duration
import java.util.Properties

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream.Materialized
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}

/** Make sure to create 2 topics before running this:
  *
  *    kafka-topics --create --zookeeper localhost:2181 --partitions 1 --replication-factor 1 --topic rawSentences
  *
  *    kafka-topics --create --zookeeper localhost:2181 --partitions 1 --replication-factor 1 --topic  countedTokens
  *
  * Then add some content in the first one:
  *
  *   kafka-console-producer --broker-list localhost:9092  --topic rawSentences
  *
  * which you can check by just tailing that topic:
  *
  *   kafka-console-consumer --bootstrap-server localhost:9092 --topic rawSentences --from-beginning
  *
  * One the programme below runs (e.g. through "sbt run"), you can simply inspect its output with:
  *
  *   kafka-console-consumer --bootstrap-server localhost:9092 --key-deserializer org.apache.kafka.common.serialization.StringDeserializer --value-deserializer org.apache.kafka.common.serialization.LongDeserializer --from-beginning --topic countedTokens
  */

object WordCount extends App {
  import Serdes._

  val props: Properties = {
    val p = new Properties()
    p.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-application")
    p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    p
  }

  val builder: StreamsBuilder = new StreamsBuilder
  builder
    .stream[String, String]("word-count-input")
    .mapValues(_.toLowerCase)
    .flatMapValues(_.split("\\W+"))
    .selectKey((_, word) => word)
    .groupByKey
    .count()(Materialized.as("counts-store"))
    .toStream
    .to("word-count-output")

  val streams: KafkaStreams = new KafkaStreams(builder.build(), props)
  streams.start()

  sys.ShutdownHookThread {
    streams.close(Duration.ofSeconds(10))
  }
}
