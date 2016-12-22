package com.github.lzenczuk.akkacrawler.util

import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.pubsub.DistributedPubSubMediator.Publish

/**
  * Created by dev on 22/12/16.
  */
trait EventBus {
  def publish(message:Any, sender:ActorRef)
}

class ClusterEventBus(actorSystem: ActorSystem, topic:String) extends EventBus {
  import akka.cluster.pubsub.DistributedPubSub

  private val mediator = DistributedPubSub(actorSystem).mediator

  override def publish(message: Any, sender: ActorRef): Unit = {
    mediator.tell(Publish(topic, message), sender)
  }
}
