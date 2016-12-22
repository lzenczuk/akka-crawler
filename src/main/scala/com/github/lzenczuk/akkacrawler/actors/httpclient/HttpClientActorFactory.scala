package com.github.lzenczuk.akkacrawler.actors.httpclient

import akka.actor.{ActorRef, ActorSystem}

/**
  * Created by dev on 22/12/16.
  */
trait HttpClientActorFactory {
  def create(id:String):ActorRef
}

class HttpClientActorFactoryImpl(actorSystem: ActorSystem) extends HttpClientActorFactory{
  override def create(id: String): ActorRef = actorSystem.actorOf(HttpClientActor.props(id), s"HttpClient$id")
}
