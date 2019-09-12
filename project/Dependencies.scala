import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
  lazy val akka_actor = "com.typesafe.akka" %% "akka-actor" % "2.5.22"
  lazy val akka_http = "com.typesafe.akka" %% "akka-http"   % "10.1.8"
  lazy val akka_http_spray_json = "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.8"
  lazy val akka_http_testkit = "com.typesafe.akka" %% "akka-http-testkit" % "10.1.8"
  lazy val akka_stream = "com.typesafe.akka" %% "akka-stream" % "2.5.22"
  lazy val akka_testkit = "com.typesafe.akka" %% "akka-testkit" % "2.5.22"
}
