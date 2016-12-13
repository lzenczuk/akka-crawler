package com.github.lzenczuk.akkacrawler.actors.notification

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
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

  var listener:Option[ActorRef] = None
  val state =  new NotificationsState

  def receive = {
    case SetNotificationListener(listenerRef) => listener = Some(listenerRef)
    case NotificationListenerTerminated => context stop self

    case SubscribeClusterNotification if state.isClusterNotificationsNotActive =>
      clusterStatusActor ! ClusterStatusActor.Subscribe(self)
      state.activateClusterNotifications()
    case CancelClusterNotificationSubscription if state.isClusterNotificationsActive =>
      clusterStatusActor ! ClusterStatusActor.UnSubscribe
      state.deactivateClusterNotifications()

    case cse:ClusterStatusEvent if state.isClusterNotificationsActive => listener.foreach(_ ! cse)
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
