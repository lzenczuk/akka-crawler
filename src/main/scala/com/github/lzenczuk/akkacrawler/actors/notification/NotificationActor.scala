package com.github.lzenczuk.akkacrawler.actors.notification

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import com.github.lzenczuk.akkacrawler.actors.cluster.ClusterStatusActor
import com.github.lzenczuk.akkacrawler.models.cluster.ClusterModels.ClusterStatus._
import com.github.lzenczuk.akkacrawler.models.notification.NotificationModels._


/**
  * Created by dev on 13/12/16.
  */

object NotificationActor {
  def props(clusterStatusActor: ActorRef) = Props(new NotificationActor(clusterStatusActor))
}

class NotificationActor(clusterStatusActor: ActorRef) extends Actor with ActorLogging{

  log.info("Starting new notifications actor.")

  var listener:Option[ActorRef] = None
  val state =  new NotificationsState

  def receive = {
    case SetNotificationListener(listenerRef) =>
      listener = Some(listenerRef)
      log.info("Listener set. Switching to ready.")
      context watch listenerRef
      context.become(ready)
    case msg =>
      log.error(s"Receive unknown message when waiting for listener: $msg.")
  }

  def ready:Receive = {
    case NotificationListenerTerminated =>
      log.info("Receive listener terminated message. Stopping.")
      context stop self
    case Terminated(ar) =>
      log.info("Listener terminated. Stopping.")
      context stop self

    case SubscribeClusterNotification if state.isClusterNotificationsNotActive =>
      log.info("Subscribing cluster notifications.")
      clusterStatusActor ! ClusterStatusActor.Subscribe(self)
      state.activateClusterNotifications()
    case CancelClusterNotificationSubscription if state.isClusterNotificationsActive =>
      log.info("Unsubscribing cluster notifications.")
      clusterStatusActor ! ClusterStatusActor.UnSubscribe
      state.deactivateClusterNotifications()

    case cse:ClusterStatusEvent if state.isClusterNotificationsActive => listener.foreach(_ ! cse)

    case msg =>
      log.error(s"Ignore message: $msg.")
  }

  log.info("Notifications actor ready. Waiting for listener.")
}

class NotificationsState{
  var activeNotifications = Map(
    "cluster" -> false
  )

  def activateClusterNotifications() = activeNotifications = activeNotifications + ("cluster" -> true)
  def deactivateClusterNotifications() = activeNotifications = activeNotifications + ("cluster" -> false)
  def isClusterNotificationsActive = activeNotifications("cluster")
  def isClusterNotificationsNotActive = !isClusterNotificationsActive
}
