package com.github.lzenczuk.akkacrawler.config.akka

import javax.inject.Singleton

import akka.actor.ActorSystem
import akka.cluster.Cluster
import com.google.inject.{AbstractModule, Provides}
import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by dev on 09/12/16.
  */

object AkkaModule extends AbstractModule{

  override def configure(): Unit = {}

  @Provides @Singleton
  def provideConfig:Config = ConfigFactory.load()

  @Provides @Singleton
  def provideActorSystem(config: Config): ActorSystem = ActorSystem("guice-actor-system", config)

  @Provides @Singleton
  def provideCluster(actorSystem: ActorSystem): Cluster = Cluster(actorSystem)

}
