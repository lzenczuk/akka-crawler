name := "akka-crawler"

organization := "com.github.lzenczuk"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  // Main dependencies
  "com.typesafe.akka" %% "akka-actor" % "2.4.14",
  "com.typesafe.akka" %% "akka-cluster" % "2.4.12",
  "com.typesafe.akka" %% "akka-http" % "10.0.0",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.0",
  "net.codingwell" %% "scala-guice" % "4.1.0",

  // Test dependencies
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.0" % "test",
  "org.mockito" % "mockito-core" % "2.3.4" % "test",

  // Integration test dependencies
  "com.github.tomakehurst" % "wiremock" % "2.4.1" % "test"
)

// Package

enablePlugins(JavaAppPackaging)

mainClass in Compile := Some("com.github.lzenczuk.akkacrawler.Application")