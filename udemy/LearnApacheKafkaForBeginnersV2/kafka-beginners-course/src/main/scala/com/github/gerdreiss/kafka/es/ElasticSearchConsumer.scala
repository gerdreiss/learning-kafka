package com.github.gerdreiss.kafka.es

import org.elasticsearch.client.RestHighLevelClient
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.elasticsearch.client.RestClient
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClientBuilder
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexRequestBuilder
import org.elasticsearch.client.Requests
import org.elasticsearch.client.RequestOptions
import com.github.gerdreiss.kafka.tutorial1.ConsumerDemoGroups

object ElasticSearchConsumer {
  def createClient: RestHighLevelClient = {
    // TODO to be configured
    val hostname = ""
    val username = ""
    val password = ""

    val credentialsProvider = new BasicCredentialsProvider
    credentialsProvider.setCredentials(
      AuthScope.ANY,
      new UsernamePasswordCredentials(username, password)
    )

    val httpHost = new HttpHost(hostname, 443, "https")
    val httpClientConfigCallback =
      new RestClientBuilder.HttpClientConfigCallback {
        override def customizeHttpClient(
            httpClientBuilder: HttpAsyncClientBuilder
          ): HttpAsyncClientBuilder =
          httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
      }
    val restClientBuilder = RestClient
      .builder(httpHost)
      .setHttpClientConfigCallback(httpClientConfigCallback)

    new RestHighLevelClient(restClientBuilder)
  }

  def main(args: Array[String]): Unit = {
    val client = createClient

    val indexResponse = client.index(indexRequest, RequestOptions.DEFAULT)

    val consumer = ConsumerDemoGroups.createConsumer

    consumer
      .poll(Duration.ofMillis(100))
      .forEach { r =>
        val indexRequest = Requests
          .indexRequest("twitter")
          .`type`("tweets")
          .source(r.value)

        val id = indexResponse.getId()

        println(id)
      }

    consumer.close()
    client.close()
  }
}
