package com.github.lzenczuk.akkacrawler.web.notification

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.github.lzenczuk.akkacrawler.actors.notification.NotificationActor
import com.github.lzenczuk.akkacrawler.models.cluster.ClusterModels.ClusterStatus._
import com.github.lzenczuk.akkacrawler.models.notification.NotificationModels._
import com.github.lzenczuk.akkacrawler.web.directives.SessionDirectives._
import spray.json._

/**
  * Created by dev on 12/12/16.
  */
object NotificationsRoute extends NotificationJsonProtocol {

  def notificationsFlow(notificationActorId:String, actorSystem: ActorSystem, clusterStatusActor: ActorRef): Flow[Message, Message, _] = {

    val notificationActor: ActorRef = actorSystem.actorOf(NotificationActor.props(clusterStatusActor), s"notification-actor-${notificationActorId}")
    val notificationActorSink = Sink.actorRef(notificationActor, NotificationListenerTerminated)
    val notificationActorSource = Source.actorRef(1, OverflowStrategy.fail).mapMaterializedValue(listenerRef => notificationActor ! SetNotificationListener(listenerRef))

    val messageToNotificationRequestFlow =
      Flow[Message]
      .map {
        case tm: TextMessage.Strict => Some(tm.getStrictText)
        case _ => None
      }
      .filter(_.isDefined)
      .map(_.get.parseJson.convertTo[NotificationCommand])

    val eventToMessageFlow = Flow[ClusterStatusEvent]
      .map(_.toJson.prettyPrint)
      .map(TextMessage.Strict)

    Flow.fromSinkAndSource(messageToNotificationRequestFlow.to(notificationActorSink), notificationActorSource.via(eventToMessageFlow))
  }

  def route(actorSystem: ActorSystem, clusterStatusActor: ActorRef): Route =
    session { sessionToken:String =>
      pathPrefix("ws") {
        pathPrefix("notifications") {
          val notificationActorId = s"connection-${System.nanoTime}-session-$sessionToken"
          handleWebSocketMessages(notificationsFlow(notificationActorId, actorSystem, clusterStatusActor))
        }
      }
    }

}
