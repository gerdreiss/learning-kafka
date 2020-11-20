package favcolor

import java.time.Duration
import java.util.Properties

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream.Materialized
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}

object FavColor extends App {
  import Serdes._

  val props: Properties = {
    val p = new Properties()
    p.put(StreamsConfig.APPLICATION_ID_CONFIG, "favcolor-application")
    p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    p
  }

  val builder: StreamsBuilder = new StreamsBuilder

  builder
    .stream[String, String]("favourite-colour-input")
    .mapValues(_.toLowerCase)
    .filter((_, value) => value.matches("[a-z]+,(red|green|blue)"))
    .selectKey((_, value) => value.take(value.indexOf(',')))
    .mapValues((key, value) => value.drop(key.length + 1))
    .to("user-keys-and-colours")

  builder
    .table[String, String]("user-keys-and-colours")
    .groupBy((_, color) => (color, color))
    .count()(Materialized.as("counts-by-color"))
    .toStream
    .to("favourite-colour-output")

  val streams: KafkaStreams = new KafkaStreams(builder.build(), props)
  streams.start()

  sys.ShutdownHookThread {
    streams.close(Duration.ofSeconds(10))
  }
}
