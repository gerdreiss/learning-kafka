package com.github.gerdreiss.kafka.utils

import java.{ util => ju }

import scala.jdk.CollectionConverters._

object Extensions {
  final implicit class MapOps[K, V](v: Map[K, V]) {
    def toJavaProperties: ju.Properties = {
      val p = new ju.Properties
      val m = v.map(e => e._1.toString -> e._2.toString()).asJava
      p.putAll(m)
      p
    }
  }
}
