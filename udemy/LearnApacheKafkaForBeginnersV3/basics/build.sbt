ThisBuild / scalaVersion     := "3.1.2"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "pro.reiss"
ThisBuild / organizationName := "reiss.pro"

Compile / run / fork := true

Global / onChangedBuildSource := ReloadOnSourceChanges
Global / semanticdbEnabled    := true // for metals

lazy val root = project
  .in(file("."))
  .aggregate(`using-kafka-clients`, `using-fs2-kafka`, `using-zio-kafka`)

lazy val `using-kafka-clients` = project
  .in(file("using-kafka-clients"))
  .settings(
    libraryDependencies += "org.apache.kafka" % "kafka-clients" % "3.1.0"
  )

lazy val `using-fs2-kafka` = project
  .in(file("using-fs2-kafka"))
  .settings(
    libraryDependencies += "com.github.fd4s" %% "fs2-kafka" % "3.0.0-M7"
  )

lazy val `using-zio-kafka` = project
  .in(file("using-zio-kafka"))
  .settings(
    libraryDependencies += "dev.zio" %% "zio-kafka" % "2.0.0-M2"
  )
