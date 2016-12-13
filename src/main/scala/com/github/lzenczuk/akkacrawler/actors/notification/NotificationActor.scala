package com.github.lzenczuk.akkacrawler.actors.notification

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.github.lzenczuk.akkacrawler.actors.cluster.ClusterStatusActor
import com.github.lzenczuk.akkacrawler.actors.notification.NotificationActor.{CancelClusterNotification, NotificationListenerTerminated, SetNotificationListener, SubscribeClusterNotification}
import com.github.lzenczuk.akkacrawler.models.cluster.ClusterEvent

/**
  * Created by dev on 13/12/16.
  */

object NotificationActor {

  def props(clusterStatusActor: ActorRef) = Props(new NotificationActor(clusterStatusActor))

  case class SetNotificationListener(listenerRef:ActorRef)
  case object NotificationListenerTerminated

  sealed trait NotificationRequest
  case object SubscribeClusterNotification extends NotificationRequest
  case object CancelClusterNotification extends NotificationRequest
}

class NotificationActor(clusterStatusActor: ActorRef) extends Actor with ActorLogging{

  var listener:Option[ActorRef] = None
  val state =  new NotificationsState

  def receive = {
    case SetNotificationListener(listenerRef) => listener = Some(listenerRef)
    case NotificationListenerTerminated => context stop self

    case SubscribeClusterNotification if state.isClusterNotificationsNotActive =>
      clusterStatusActor ! ClusterStatusActor.Subscribe(self)
      state.activateClusterNotifications()
    case CancelClusterNotification if state.isClusterNotificationsActive =>
      clusterStatusActor ! ClusterStatusActor.UnSubscribe
      state.deactivateClusterNotifications()

    case ce:ClusterEvent if state.isClusterNotificationsActive => listener.foreach(_ ! ce)
  }
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
