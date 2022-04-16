import com.launchdarkly.eventsource.{ EventHandler, MessageEvent }
import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerRecord }
import org.slf4j.LoggerFactory

class WikimediaChangeHandler(private val producer: KafkaProducer[String, String], private val topic: String) extends EventHandler:

  private val logger = LoggerFactory.getLogger(getClass)

  override def onOpen(): Unit =
    logger.info("Connected to Wikimedia change stream")

  override def onClosed(): Unit =
    producer.close()
    logger.info("Disconnected from Wikimedia change stream")

  override def onMessage(event: String, messageEvent: MessageEvent): Unit =
    logger.info(s"Received message: ${messageEvent.getData}")
    producer.send(new ProducerRecord[String, String](topic, event, messageEvent.getData))

  override def onComment(comment: String): Unit =
    logger.info(s"Received comment: $comment")

  override def onError(t: Throwable): Unit =
    logger.error("Error in Wikimedia change stream", t)
