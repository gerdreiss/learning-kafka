import sbt._

object Dependencies {
  case object com {
    case object fasterxml {
      case object jackson {
        case object core {
          val `jackson-databind` =
            "com.fasterxml.jackson.core" % "jackson-databind" % "2.11.3"
        }
      }
    }

    case object github {
      case object alexarchambault {
        val `scalacheck-shapeless_1.14` =
          "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % "1.2.5"
      }
    }

    case object olegpy {
      val `better-monadic-for` =
        "com.olegpy" %% "better-monadic-for" % "0.3.1"
    }
  }

  case object org {
    case object augustjune {
      val `context-applied` =
        "org.augustjune" %% "context-applied" % "0.1.4"
    }

    case object elasticsearch {
      case object client {
        val `elasticsearch-rest-high-level-client` =
          "org.elasticsearch.client" % "elasticsearch-rest-high-level-client" % "7.9.3"
      }
    }

    case object kafka {
      val `kafka-clients` =
        "org.apache.kafka" % "kafka-clients" % "2.6.0"
    }

    case object slf4j {
      val `slf4j-simple` =
        "org.slf4j" % "slf4j-simple" % "1.7.30"
    }

    case object scalacheck {
      val scalacheck =
        "org.scalacheck" %% "scalacheck" % "1.14.3"
    }

    case object scalatest {
      val scalatest =
        "org.scalatest" %% "scalatest" % "3.2.2"
    }

    case object scalatestplus {
      val `scalacheck-1-14` =
        "org.scalatestplus" %% "scalacheck-1-14" % "3.2.2.0"
    }

    case object typelevel {
      val `discipline-scalatest` =
        "org.typelevel" %% "discipline-scalatest" % "2.0.1"

      val `kind-projector` =
        "org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full
    }
  }
}
