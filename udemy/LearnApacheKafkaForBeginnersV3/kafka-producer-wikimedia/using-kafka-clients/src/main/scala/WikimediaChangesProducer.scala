import com.launchdarkly.eventsource.EventSource
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig

import java.net.URI
import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters.*

object WikimediaChangesProducer extends App:

  val properties = Map(
    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG      -> "localhost:9092",
    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG   -> "org.apache.kafka.common.serialization.StringSerializer",
    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringSerializer",

    // safe producer config for Kafka <= 2.8
    ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG             -> "true",
    ProducerConfig.ACKS_CONFIG                           -> "all",
    ProducerConfig.RETRIES_CONFIG                        -> Integer.MAX_VALUE.toString,
    ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION -> "5",
    ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG            -> "120000",

    // high throughput producer (at the expence of a bit of latency and CPU usage)
    ProducerConfig.COMPRESSION_TYPE_CONFIG -> "snappy",
    ProducerConfig.LINGER_MS_CONFIG        -> "20",
    ProducerConfig.BATCH_SIZE_CONFIG       -> Integer.toString(32 * 1024)
  )

  val producer     = new KafkaProducer[String, String](properties.asJava)
  val topic        = "wikimedia-recent-changes"
  val eventHandler = new WikimediaChangeHandler(producer, topic)
  val url          = new URI("https://stream.wikimedia.org/v2/stream/recentchange")

  new EventSource.Builder(eventHandler, url).nn.build().nn.start()

  TimeUnit.MINUTES.sleep(10)
