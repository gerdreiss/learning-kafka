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
import java.{ util => ju }
import java.time.Duration
import com.google.gson.JsonParser
import org.elasticsearch.action.bulk.BulkRequest

import scala.jdk.CollectionConverters._

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

    val httpHost                 = new HttpHost(hostname, 443, "https")
    val httpClientConfigCallback =
      new RestClientBuilder.HttpClientConfigCallback {
        override def customizeHttpClient(
            httpClientBuilder: HttpAsyncClientBuilder
          ): HttpAsyncClientBuilder =
          httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
      }
    val restClientBuilder        = RestClient
      .builder(httpHost)
      .setHttpClientConfigCallback(httpClientConfigCallback)

    new RestHighLevelClient(restClientBuilder)
  }

  def extractTweetId(tweet: String): Option[String] =
    Option(JsonParser.parseString(tweet).getAsJsonObject().get("id_str"))
      .map(_.getAsString())

  def main(args: Array[String]): Unit = {
    val client      = createClient
    val consumer    = ConsumerDemoGroups.createConsumer
    val bulkRequest = consumer
      .poll(Duration.ofMillis(100))
      .iterator()
      .asScala
      .map(_.value())
      .map { v =>
        extractTweetId(v).map((_, v))
      }
      .filter(_.isDefined)
      .map { t =>
        Requests
          .indexRequest("twitter")
          // this is deprecated and probably not necessary
          //.`type`("tweets")
          // set unique record id for idempotency
          //.id(s"${r.topic()}_${r.partition()}_${r.offset()}")
          // or better
          .id(t.get._1)
          .source(t.get._2)
      }
      .foldLeft(new BulkRequest)(_ add _)
    //   .forEach { r =>
    //     val indexRequest = Requests
    //       .indexRequest("twitter")
    //       .`type`("tweets")
    //       // set unique record id for idempotency
    //       //.id(s"${r.topic()}_${r.partition()}_${r.offset()}")
    //       // or better
    //       .id(extractTweetId(r.value()))
    //       .source(r.value)
    //     bulkRequest.add(indexRequest)
    //   // val indexResponse = client.index(indexRequest, RequestOptions.DEFAULT)
    //   // val id = indexResponse.getId()
    //   // println(id)
    //   }
    // check if requests have been added - otherwise exception is thrown
    if (bulkRequest.numberOfActions() > 0) {
      client.bulk(bulkRequest, RequestOptions.DEFAULT)
      consumer.commitSync()
    }
    consumer.close()
    client.close()
  }
}
