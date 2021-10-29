import Dependencies._

ThisBuild / organization := "com.github.gerdreiss"
ThisBuild / scalaVersion := "3.1.0"

ThisBuild / scalacOptions ++=
  Seq(
    "-deprecation",
    "-feature",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings",
    //"-Yexplicit-nulls", // experimental (I've seen it cause issues with circe)
    "-Ykind-projector",
    //"-Ysafe-init", // experimental (I've seen it cause issues with circe)
  ) ++ Seq("-rewrite", "-indent") ++ Seq("-source", "future")

lazy val `kafka-streams-101` =
  project
    .in(file("."))
    .settings(name := "kafka-streams-101")
    .settings(commonSettings)
    .settings(dependencies)

lazy val commonSettings = commonScalacOptions ++ Seq(
  update / evictionWarningOptions := EvictionWarningOptions.empty
)

lazy val commonScalacOptions = Seq(
  Compile / console / scalacOptions --= Seq(
    "-Wunused:_",
    "-Xfatal-warnings",
  ),
  Test / console / scalacOptions :=
    (Compile / console / scalacOptions).value,
)

lazy val dependencies = Seq(
  libraryDependencies ++= Seq(
    org.apache.kafka.`kafka-clients`,
    org.apache.kafka.`kafka-streams`,
    org.apache.kafka.`kafka-streams-scala`,
    io_.circe.`circe-core`,
    io_.circe.`circe-generic`,
    io_.circe.`circe-parser`,
  ),
  libraryDependencies ++= Seq(
    org.scalatest.scalatest,
    org.scalatestplus.`scalacheck-1-15`,
  ).map(_ % Test),
)
