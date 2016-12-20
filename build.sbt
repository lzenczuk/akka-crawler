name := "akka-crawler"

organization := "com.github.lzenczuk"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  // Main dependencies
  "com.typesafe.akka" %% "akka-actor" % "2.4.14",

  "com.typesafe.akka" %% "akka-cluster" % "2.4.14",

  "com.typesafe.akka" %% "akka-http" % "10.0.0",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.0",

  "com.typesafe.akka" %% "akka-persistence" % "2.4.14",
  "org.iq80.leveldb" % "leveldb" % "0.7",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",

  "net.codingwell" %% "scala-guice" % "4.1.0",

  // Test dependencies
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.mockito" % "mockito-core" % "2.3.4" % "test",

  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.0" % "test",

  "com.github.tomakehurst" % "wiremock" % "2.4.1" % "test",

  "com.github.dnvriend" %% "akka-persistence-inmemory" % "1.3.17" % "test"
)

// Package

enablePlugins(JavaAppPackaging)

mainClass in Compile := Some("com.github.lzenczuk.akkacrawler.Application")