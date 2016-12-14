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
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.0",

  // Integration test dependencies
  "org.mock-server" % "mockserver-client" % "3.2"
)

// Package

enablePlugins(JavaAppPackaging)

mainClass in Compile := Some("com.github.lzenczuk.akkacrawler.Application")