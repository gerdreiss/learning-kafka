package kafkastreams101

import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.{ Decoder, Encoder }
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.scala.serialization.Serdes

object Domain:
  opaque type UserId = String
  opaque type Profile = String
  opaque type Product = String
  opaque type OrderId = String

  case class Order(
      orderId: OrderId,
      user: UserId,
      products: List[Product],
      amount: BigDecimal,
    )
  case class Discount(profile: Profile, amount: Float) // in percentage points
  case class Payment(orderId: OrderId, status: String)

  given Serde[UserId] = serde[UserId]
  given Serde[Profile] = serde[Profile]
  given Serde[Product] = serde[Product]
  given Serde[OrderId] = serde[OrderId]
  given Serde[Order] = serde[Order]
  given Serde[Discount] = serde[Discount]
  given Serde[Payment] = serde[Payment]

  private def serde[T >: Null: Decoder: Encoder]: Serde[T] = Serdes.fromFn(
    (data: T) => data.asJson.noSpaces.getBytes,
    (data: Array[Byte]) => decode[T](data.mkString).toOption,
  )

object Topics:
  final val OrdersByUserTopic = "orders-by-user"
  final val DiscountProfilesByUserTopic = "discount-profiles-by-user"
  final val DiscountsTopic = "discounts"
  final val OrdersTopic = "orders"
  final val PaymentsTopic = "payments"
  final val PaidOrdersTopic = "paid-orders"
