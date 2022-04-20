import com.google.gson.JsonParser
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.opensearch.action.bulk.BulkRequest
import org.opensearch.action.index.IndexRequest
import org.opensearch.client.RequestOptions
import org.opensearch.common.xcontent.XContentType
import org.slf4j.LoggerFactory

import java.time.Duration as JDuration
import java.util.List as JList
import scala.jdk.CollectionConverters.*
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.util.Using

object OpenSearchConsumer extends App:

  val logger = LoggerFactory.getLogger(getClass)

  given Using.Releasable[KafkaConsumer[?, ?]] with
    def release(consumer: KafkaConsumer[?, ?]): Unit = consumer.close()

  def extractId(json: String): Try[String] = Try {
    JsonParser
      .parseString(json)
      .getAsJsonObject
      .get("meta")
      .getAsJsonObject
      .get("id")
      .getAsString
  }

  Using.resources(OpenSearchClient.make, WikimediaRecentChangesConsumer.make) {
    (client, consumer) =>

      import OpenSearchClient.*

      client.initializeIndex("wikimedia-recent-changes")
      consumer.subscribe(JList.of("wikimedia-recent-changes"))

      while true do
        val bulkRequest = consumer
          .poll(JDuration.ofMillis(3000))
          .asScala
          .map { record =>
            extractId(record.value)
              .map(id =>
                new IndexRequest("wikimedia-recent-changes")
                  .id(id)
                  .source(record.value, XContentType.JSON)
              )
          }
          .foldLeft(new BulkRequest)((bulkRequest, indexRequest) =>
            indexRequest match
              case Success(indexRequest) =>
                bulkRequest.add(indexRequest)
              case Failure(exception)    =>
                logger.error("Failed to extract id from record", exception)
                bulkRequest
          )

        if bulkRequest.numberOfActions > 0 then
          val bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT)
          logger.info(s"Inserted ${bulkResponse.getItems.size} record(s)")

          consumer.commitAsync()
          logger.info("Offsets committed")

          Thread.sleep(1000)
  }
