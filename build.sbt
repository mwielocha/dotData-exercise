
// Generated with scalagen

lazy val root = (project in file(".")).
  settings(
    name := "dotData-exercise",
    version := "1.0",
    scalaVersion := "2.13.1"
  )

mainClass in (Compile, run) := Some("io.mwielocha.scheduler.SchedulerMain")

val akkaVersion = "2.6.4"
val circeVersion = "0.13.0"
val logbackVersion = "1.2.3"
val akkaHttpVersion = "10.1.11"
val shapelessVersion = "2.3.3"
val scalatestVersion = "3.1.1"
val akkaHttpCirceVersion = "1.31.0"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "com.chuusai" %% "shapeless" % shapelessVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "org.scalatest" %% "scalatest" % scalatestVersion % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test
)

