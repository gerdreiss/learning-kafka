import com.google.gson.JsonParser
import org.apache.http.HttpHost
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.opensearch.action.index.IndexRequest
import org.opensearch.client.RequestOptions
import org.opensearch.client.RestClient
import org.opensearch.client.RestHighLevelClient
import org.opensearch.client.indices.CreateIndexRequest
import org.opensearch.client.indices.GetIndexRequest
import org.opensearch.common.xcontent.XContentType
import org.slf4j.LoggerFactory

import java.time.Duration as JDuration
import scala.jdk.CollectionConverters.*
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.util.Using

object OpenSearchConsumer extends App:

  val logger = LoggerFactory.getLogger(getClass)

  given Using.Releasable[KafkaConsumer[?, ?]] with
    def release(consumer: KafkaConsumer[?, ?]): Unit = consumer.close()

  val openSearchClient =
    new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))

  val kafkaConsumer = new KafkaConsumer[String, String](
    Map(
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG        -> "localhost:9092",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG   -> classOf[StringDeserializer].getName,
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer].getName,
      ConsumerConfig.GROUP_ID_CONFIG                 -> "opensearch-consumer",
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG        -> "latest"
    ).asJava
  )

  Using.resources(openSearchClient, kafkaConsumer) { (client, consumer) =>
    if client.indices
        .exists(
          new GetIndexRequest("wikimedia-recent-changes"),
          RequestOptions.DEFAULT
        )
    then logger.info("Index already exists")
    else
      val response = client.indices
        .create(
          new CreateIndexRequest("wikimedia-recent-changes"),
          RequestOptions.DEFAULT
        )
      logger.info("Index created: " + response.index)

    consumer.subscribe(List("wikimedia-recent-changes").asJava)

    while true do
      consumer
        .poll(JDuration.ofMillis(3000))
        .asScala
        .map { record =>
          // val id = record.topic + "-" + record.partition + "-" + record.offset
          val id = JsonParser
            .parseString(record.value)
            .getAsJsonObject
            .get("meta")
            .getAsJsonObject
            .get("id")
            .getAsString

          val request = new IndexRequest("wikimedia-recent-changes")
            .source(record.value, XContentType.JSON)
            .id(id)

          Try(client.index(request, RequestOptions.DEFAULT))
        }
        .foreach {
          case Success(response) =>
            logger.info(s"Indexed record ${response.getId}")
          case Failure(error)    =>
            logger.error(s"Error indexing record", error)
        }
  }
