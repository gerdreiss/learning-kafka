import org.apache.http.HttpHost
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.opensearch.client.RequestOptions
import org.opensearch.client.RestClient
import org.opensearch.client.RestHighLevelClient
import org.opensearch.client.indices.CreateIndexRequest
import org.opensearch.client.indices.GetIndexRequest
import org.slf4j.LoggerFactory

import java.net.URI
import scala.jdk.CollectionConverters.*
import scala.util.Using
import scala.util.Using.Releasable

import java.time.Duration as JDuration
import org.opensearch.action.index.IndexRequest
import org.opensearch.common.xcontent.XContentType
import scala.util.Try
import org.opensearch.action.index.IndexResponse

object OpenSearchConsumer extends App:

  val logger = LoggerFactory.getLogger(getClass)

  given Releasable[KafkaConsumer[?, ?]] with
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
      client.indices
        .create(
          new CreateIndexRequest("wikimedia-recent-changes"),
          RequestOptions.DEFAULT
        )
      logger.info("Index created")

    consumer.subscribe(List("wikimedia-recent-changes").asJava)

    while true do
      consumer
        .poll(JDuration.ofMillis(3000))
        .asScala
        .map { record =>
          Try(
            client.index(
              new IndexRequest("wikimedia-recent-changes")
                .source(record.value(), XContentType.JSON),
              RequestOptions.DEFAULT
            )
          ).recover(_ => new IndexResponse.Builder().build())
        }
        .foreach(response => logger.info(response.toString))
  }
