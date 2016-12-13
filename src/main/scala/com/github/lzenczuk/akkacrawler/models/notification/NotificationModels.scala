package com.github.lzenczuk.akkacrawler.models.notification

import akka.actor.ActorRef

/**
  * Created by dev on 13/12/16.
  */
object NotificationModels {

  trait NotificationActorCommand
  case class SetNotificationListener(listenerRef:ActorRef) extends NotificationActorCommand
  case object NotificationListenerTerminated extends NotificationActorCommand

  trait NotificationCommand
  case object SubscribeClusterNotification extends NotificationCommand
  case object CancelClusterNotificationSubscription extends NotificationCommand

}
