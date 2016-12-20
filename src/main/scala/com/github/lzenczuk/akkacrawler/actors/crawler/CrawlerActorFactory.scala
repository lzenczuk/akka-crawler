package com.github.lzenczuk.akkacrawler.actors.crawler

import akka.actor.{ActorRef, ActorSystem}

/**
  * Created by dev on 20/12/16.
  */
trait CrawlerActorFactory {
  def create(requestId:String):ActorRef
}

class CrawlerActorFactoryImpl(system:ActorSystem) extends CrawlerActorFactory {
  override def create(requestId: String): ActorRef = system.actorOf(CrawlerActor.props(requestId))
}
