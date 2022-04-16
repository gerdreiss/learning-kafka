import com.launchdarkly.eventsource.{ EventHandler, EventSource, MessageEvent }
import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerConfig }

import java.net.URI
import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters.*

object WikimediaChangesProducer extends App:

  val properties = Map(
    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG      -> "localhost:9092",
    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG   -> "org.apache.kafka.common.serialization.StringSerializer",
    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringSerializer"
  )

  val producer     = new KafkaProducer[String, String](properties.asJava)
  val topic        = "wikipedia-recent-changes"
  val eventHandler = new WikimediaChangeHandler(producer, topic)
  val url          = new URI("https://stream.wikimedia.org/v2/stream/recentchange")

  new EventSource.Builder(eventHandler, url).build().start()

  TimeUnit.MINUTES.sleep(10)
