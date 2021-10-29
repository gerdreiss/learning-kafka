import sbt._

object Dependencies {

  val circeVersion = "0.14.1"
  val kafkaVersion = "3.0.0"

  case object io_ { // to prevent ambigious import error
    case object circe {
      val `circe-core` =
        "io.circe" %% "circe-core" % circeVersion
      val `circe-generic` =
        "io.circe" %% "circe-generic" % circeVersion
      val `circe-parser` =
        "io.circe" %% "circe-parser" % circeVersion
    }
  }
  case object org {
    case object apache {
      case object kafka {
        val kafka =
          "org.apache.kafka" %% "kafka" % kafkaVersion cross CrossVersion.for3Use2_13
        val `kafka-clients` =
          "org.apache.kafka" % "kafka-clients" % kafkaVersion
        val `kafka-streams` =
          "org.apache.kafka" % "kafka-streams" % kafkaVersion
        val `kafka-streams-scala` =
          "org.apache.kafka" %% "kafka-streams-scala" % kafkaVersion cross CrossVersion.for3Use2_13
      }
    }

    case object scalatest {
      val scalatest =
        "org.scalatest" %% "scalatest" % "3.2.10"
    }

    case object scalatestplus {
      val `scalacheck-1-15` =
        "org.scalatestplus" %% "scalacheck-1-15" % "3.2.10.0"
    }
  }
}
