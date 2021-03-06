name := "akka-crawler"

organization := "com.github.lzenczuk"

version := "1.0"

scalaVersion := "2.12.1"

val root = (project in file(".")).enablePlugins(JavaAppPackaging)

mainClass in Compile := Some("com.github.lzenczuk.akkacrawler.Application")

libraryDependencies ++= Seq(
  "com.typesafe.akka"         %%  "akka-actor"                          % "2.4.14",
  "com.typesafe.akka"         %%  "akka-cluster"                        % "2.4.14",
  "com.typesafe.akka"         %%  "akka-cluster-tools"                  % "2.4.14",
  "com.typesafe.akka"         %%  "akka-persistence"                    % "2.4.14",
  "com.typesafe.akka"         %%  "akka-persistence-query-experimental" % "2.4.14",
  "com.typesafe.akka"         %%  "akka-http"                           % "10.0.0",
  "com.typesafe.akka"         %%  "akka-http-spray-json"                % "10.0.0",
  "net.codingwell"            %%  "scala-guice"                         % "4.1.0",
  "org.iq80.leveldb"          %   "leveldb"                             % "0.7",
  "org.fusesource.leveldbjni" %   "leveldbjni-all"                      % "1.8",

  "org.scalatest"             %%  "scalatest"                           % "3.0.1"     % "test",
  "org.mockito"               %   "mockito-core"                        % "2.3.4"     % "test",
  "com.typesafe.akka"         %%  "akka-http-testkit"                   % "10.0.0"    % "test",
  "com.github.tomakehurst"    %   "wiremock"                            % "2.4.1"     % "test",
  "com.github.dnvriend"       %%  "akka-persistence-inmemory"           % "1.3.17"    % "test"
)
