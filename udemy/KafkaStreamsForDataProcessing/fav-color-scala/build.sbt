
lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(
      organization := "com.github.gerdreiss",
      scalaVersion := "2.13.4",
      version := "0.1.0-SNAPSHOT"
    )
  ),
  name := "fav-color",
  libraryDependencies ++= Seq(
    "org.apache.kafka" %% "kafka-streams-scala" % "2.6.0",
    "org.slf4j"         % "slf4j-simple"        % "1.7.25",
    "org.scalatest"    %% "scalatest"           % "3.2.3"  % Test,
    "org.scalacheck"   %% "scalacheck"          % "1.15.1" % Test
  )
)
