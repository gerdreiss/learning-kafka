import org.apache.http.HttpHost
import org.opensearch.client.RequestOptions
import org.opensearch.client.RestClient
import org.opensearch.client.RestHighLevelClient
import org.opensearch.client.indices.CreateIndexRequest
import org.opensearch.client.indices.GetIndexRequest
import org.slf4j.LoggerFactory

import scala.util.Try
import scala.util.Using

object OpenSearchClient:

  private val logger = LoggerFactory.getLogger(getClass)

  def make: RestHighLevelClient =
    new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))

  extension (client: RestHighLevelClient)
    def initializeIndex(name: String): Unit =
      if client.indices
          .exists(
            new GetIndexRequest(name),
            RequestOptions.DEFAULT
          )
      then logger.info("Index already exists")
      else
        val response = client.indices
          .create(
            new CreateIndexRequest(name),
            RequestOptions.DEFAULT
          )
        logger.info("Index created: " + response.index)
