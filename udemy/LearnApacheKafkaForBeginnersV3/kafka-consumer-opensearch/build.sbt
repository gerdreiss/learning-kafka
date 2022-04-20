ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.1.2"

lazy val `kafka-consumer-opensearch` = project
  .in(file("."))
  .aggregate(`using-kafka-clients-opensearch`, `using-fs2-kafka`, `using-zio-kafka-elasticsearch`)

lazy val `using-kafka-clients-opensearch` = project
  .in(file("using-kafka-clients-opensearch"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.opensearch.client" % "opensearch-rest-high-level-client" % "1.3.1",
      "com.google.code.gson"  % "gson"                              % "2.9.0",
      "org.apache.kafka"      % "kafka-clients"                     % "3.1.0",
      "org.slf4j"             % "slf4j-simple"                      % "1.7.36"
    )
  )

lazy val `using-fs2-kafka` = project
  .in(file("using-fs2-kafka"))
  .settings(commonSettings)
  .settings(
    resolvers += "Confluent IO" at "https://packages.confluent.io/maven/",
    libraryDependencies ++= Seq(
      "com.github.fd4s" %% "fs2-kafka"   % "3.0.0-M7",
      "org.typelevel"   %% "cats-effect" % "3.3.11"
    )
  )

lazy val `using-zio-kafka-elasticsearch` = project
  .in(file("using-zio-kafka-elasticsearch"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio"                %% "zio-kafka"               % "2.0.0-M3",
      "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % "8.1.0" cross CrossVersion.for3Use2_13
    )
  )

val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-source:future",
    "-deprecation",     // emit warning and location for usages of deprecated APIs
    "-explain",         // explain errors in more detail
    "-explain-types",   // explain type errors in more detail
    "-feature",         // emit warning and location for usages of features that should be imported explicitly
    "-indent",          // allow significant indentation.
    "-new-syntax",      // require `then` and `do` in control expressions.
    "-print-lines",     // show source code line numbers.
    "-unchecked",       // enable additional warnings where generated code depends on assumptions
    "-Ykind-projector", // allow `*` as wildcard to be compatible with kind projector
    "-Xfatal-warnings", // fail the compilation if there are any warnings
    "-Xmigration"       // warn about constructs whose behavior may have changed since version
  )
)
