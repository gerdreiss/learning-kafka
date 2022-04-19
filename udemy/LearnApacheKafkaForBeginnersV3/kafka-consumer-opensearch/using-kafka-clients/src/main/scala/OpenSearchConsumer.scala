import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy
import org.opensearch.client.RequestOptions
import org.opensearch.client.RestClient
import org.opensearch.client.RestHighLevelClient
import org.opensearch.client.indices.CreateIndexRequest
import org.opensearch.client.indices.GetIndexRequest
import org.slf4j.LoggerFactory

import java.net.URI
import scala.util.Using

object OpenSearchConsumer extends App:
  val logger = LoggerFactory.getLogger(getClass)

  val openSearchClient =
    new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))

  Using(openSearchClient) { client =>
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
  }
