package kafkastreams101

import org.apache.kafka.streams.scala.ImplicitConversions.*
import org.apache.kafka.streams.scala.StreamsBuilder

import Domain.given
import Domain.*
import Topics.*

object Main extends App:

  // topology
  val builder = StreamsBuilder()

  // KStream
  val userOrderStream = builder.stream[UserId, Order](OrdersByUserTopic)

  // KTable - is distributed
  val userProfileTable = builder.table[UserId, Profile](DiscountProfilesByUserTopic)

  // GlobalKTable - copied to all the nodes
  val discountProfilesGTable = builder.globalTable[Profile, Discount](DiscountsTopic)

  // KStream transformation
  val expensiveOrders = userOrderStream.filter((_, order) => order.amount > 1000)

  val listsOfProducts = userOrderStream.mapValues(_.products)

  val productsStream = userOrderStream.flatMapValues(_.products)

  // join
  val ordersWithUserProfiles = userOrderStream.join(userProfileTable)((order, profile) => (order, profile))

  val discountedOrdersStream = ordersWithUserProfiles.join(discountProfilesGTable)(
    { case (user, (order, profile)) => profile },
    { case ((order, profile), discount) => order.copy(amount = order.amount - discount) },
  )

  builder.build

  println("─" * 100)
  println("hello world")
  println("─" * 100)
