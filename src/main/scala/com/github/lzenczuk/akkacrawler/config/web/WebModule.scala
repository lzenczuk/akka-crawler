package com.github.lzenczuk.akkacrawler.config.web

import javax.inject.{Named, Singleton}

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.lzenczuk.akkacrawler.web.cluster.ClusterRoute
import com.github.lzenczuk.akkacrawler.web.notification.NotificationsRoute
import com.google.inject.{AbstractModule, Provides}

/**
  * Created by dev on 09/12/16.
  */

object WebModule extends AbstractModule {

  override def configure(): Unit = {}

  @Provides @Singleton
  def providesRoutes(
                      actorSystem:ActorSystem,
                      @Named("cluster-manger") clusterManagerActor:ActorRef,
                      @Named("cluster-status") clusterStatusActor:ActorRef
                    ):Route = {
    ClusterRoute.route(actorSystem, clusterManagerActor) ~
    NotificationsRoute.route(actorSystem, clusterStatusActor)
  }
}
