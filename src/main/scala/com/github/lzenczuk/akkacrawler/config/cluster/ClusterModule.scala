package com.github.lzenczuk.akkacrawler.config.cluster

import javax.inject.{Named, Singleton}

import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.Cluster
import com.github.lzenczuk.akkacrawler.actors.cluster.ClusterStatusActor
import com.github.lzenczuk.akkacrawler.service.cluster.{ClusterService, ClusterServiceImpl}
import com.google.inject.{AbstractModule, Provides}

/**
  * Created by dev on 08/12/16.
  */

object ClusterModule extends AbstractModule{

  override def configure(): Unit = {}

  @Provides @Singleton
  def providesClusterService(cluster:Cluster):ClusterService = new ClusterServiceImpl(cluster)

  @Provides @Singleton @Named("cluster-status")
  def providesClusterStatusActor(actorSystem: ActorSystem, cluster:Cluster):ActorRef = actorSystem.actorOf(ClusterStatusActor.props(cluster))
}
