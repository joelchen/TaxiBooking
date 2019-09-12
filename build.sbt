import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "com.joel"
ThisBuild / organizationName := "Joel"

lazy val root = (project in file("."))
  .settings(
    name := "TaxiBooking",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += akka_actor,
    libraryDependencies += akka_http,
    libraryDependencies += akka_http_spray_json,
    libraryDependencies += akka_http_testkit,
    libraryDependencies += akka_stream,
    libraryDependencies += akka_testkit
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
