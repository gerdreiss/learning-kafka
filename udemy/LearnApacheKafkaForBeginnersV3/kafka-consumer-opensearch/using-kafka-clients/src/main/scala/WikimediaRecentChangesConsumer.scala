import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer

import scala.jdk.CollectionConverters.*

object WikimediaRecentChangesConsumer:
  def make: KafkaConsumer[String, String] =
    new KafkaConsumer[String, String](
      Map(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG        -> "localhost:9092",
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG   -> classOf[StringDeserializer].getName,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer].getName,
        ConsumerConfig.GROUP_ID_CONFIG                 -> "opensearch-consumer",
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG        -> "latest",
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG       -> "false"
      ).asJava
    )
