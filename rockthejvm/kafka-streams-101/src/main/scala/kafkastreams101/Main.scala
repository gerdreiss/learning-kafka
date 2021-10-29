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

  println("─" * 100)
  println("hello world")
  println("─" * 100)
