ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.2"

lazy val `kafka-producer-wikimedia` = project
  .in(file("."))
  .aggregate(`using-kafka-clients`, `using-fs2-kafka`, `using-zio-kafka`)

lazy val `using-kafka-clients` = project
  .in(file("using-kafka-clients"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.kafka"     % "kafka-clients"      % "3.1.0",
      "org.slf4j"            % "slf4j-api"          % "1.7.36",
      "org.slf4j"            % "slf4j-simple"       % "1.7.36",
      "com.squareup.okhttp3" % "okhttp"             % "4.9.3",
      "com.launchdarkly"     % "okhttp-eventsource" % "2.5.0"
    )
  )

lazy val `using-fs2-kafka` = project
  .in(file("using-fs2-kafka"))
  .settings(commonSettings)
  .settings(
    resolvers += "Confluent IO" at "https://packages.confluent.io/maven/",
    libraryDependencies ++= Seq(
      "org.http4s"      %% "http4s-ember-client" % "1.0.0-M23",
      "org.http4s"      %% "http4s-circe"        % "1.0.0-M23",
      "io.circe"        %% "circe-generic"       % "0.15.0-M1",
      "com.github.fd4s" %% "fs2-kafka"           % "3.0.0-M7",
      "com.github.fd4s" %% "fs2-kafka-vulcan"    % "3.0.0-M7",
      "org.typelevel"   %% "cats-effect"         % "3.3.11"
    )
  )

lazy val `using-zio-kafka` = project
  .in(file("using-zio-kafka"))
  .settings(commonSettings)
  .settings(
    libraryDependencies += "dev.zio" %% "zio-kafka" % "2.0.0-M2"
  )

val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-source:future",
    // "-language:implicitConversions", // do we need this???
    // "-Yexplicit-nulls", // experimental (might cause issues with circe)
    // "-Ysafe-init", // experimental (might cause issues with circe)
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
