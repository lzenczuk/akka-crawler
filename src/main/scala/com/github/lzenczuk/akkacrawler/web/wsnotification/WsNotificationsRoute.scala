package com.github.lzenczuk.akkacrawler.web.wsnotification

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.github.lzenczuk.akkacrawler.actors.notification.NotificationActor
import com.github.lzenczuk.akkacrawler.actors.notification.NotificationActor._
import com.github.lzenczuk.akkacrawler.models.cluster._
import spray.json._

/**
  * Created by dev on 12/12/16.
  */
object WsNotificationsRoute extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val addressFormat = jsonFormat3(Address)

  implicit object NodeStatusFormat extends RootJsonFormat[NodeStatus] {
    override def write(obj: NodeStatus): JsValue = {
      obj match {
        case Joining => JsString("Joining")
        case WeaklyUp => JsString("WeaklyUp")
        case Up => JsString("Up")
        case Leaving => JsString("Leaving")
        case Exiting => JsString("Exiting")
        case Removed => JsString("Removed")
      }
    }

    override def read(json: JsValue): NodeStatus = {
      json.toString() match {
        case "Joining" => Joining
        case "WeaklyUp" => WeaklyUp
        case "Up" => Up
        case "Leaving" => Leaving
        case "Exiting" => Exiting
        case "Removed" => Removed
      }
    }
  }

  implicit val nodeFormat = jsonFormat3(Node)
  implicit val clusterFormat = jsonFormat2(Cluster)

  implicit val clusterInitEventFormat = jsonFormat1(ClusterInitEvent)
  implicit val nodeUpdateEventFormat = jsonFormat3(NodeUpdateEvent)
  implicit val masterUpdateEventFormat = jsonFormat1(MasterUpdateEvent)

  implicit object ClusterEventFormat extends RootJsonFormat[ClusterEvent] {
    override def write(obj: ClusterEvent): JsValue = {
      obj match {
        case cie: ClusterInitEvent => toJsObjectWithType("ClusterInitEvent", cie.toJson)
        case nue: NodeUpdateEvent => toJsObjectWithType("NodeUpdateEvent", nue.toJson)
        case mue: MasterUpdateEvent => toJsObjectWithType("MasterUpdateEvent", mue.toJson)
      }
    }

    def toJsObjectWithType(typeName: String, jsValue: JsValue): JsObject = JsObject(
      "type" -> JsString(typeName),
      "body" -> jsValue
    )

    override def read(value: JsValue): ClusterEvent = {
      value.asJsObject.getFields("type", "body") match {
        case Seq(JsString("ClusterInitEvent"), body: JsObject) => body.convertTo[ClusterInitEvent]
        case Seq(JsString("NodeUpdateEvent"), body: JsObject) => body.convertTo[NodeUpdateEvent]
        case Seq(JsString("MasterUpdateEvent"), body: JsObject) => body.convertTo[MasterUpdateEvent]
      }
    }
  }

  implicit object NotificationRequestFormat extends RootJsonFormat[NotificationRequest] {
    override def write(obj: NotificationRequest): JsValue = {
      obj match {
        case SubscribeClusterNotification => toJsObjectWithType("SubscribeClusterNotification")
        case CancelClusterNotification => toJsObjectWithType("CancelClusterNotification")
      }
    }

    def toJsObjectWithType(typeName: String): JsObject = JsObject(
      "type" -> JsString(typeName)
    )

    override def read(value: JsValue): NotificationRequest = {
      value.asJsObject.getFields("type") match {
        case Seq(JsString("SubscribeClusterNotification")) => SubscribeClusterNotification
        case Seq(JsString("CancelClusterNotification")) => CancelClusterNotification
        case x => println(s"receive $x")
          CancelClusterNotification
      }
    }
  }


  def notificationsFlow(actorSystem: ActorSystem, clusterStatusActor: ActorRef): Flow[Message, Message, _] = {

    val notificationActor: ActorRef = actorSystem.actorOf(NotificationActor.props(clusterStatusActor))
    val notificationActorSink = Sink.actorRef(notificationActor, NotificationListenerTerminated)
    val notificationActorSource = Source.actorRef(1, OverflowStrategy.fail).mapMaterializedValue(listenerRef => notificationActor ! SetNotificationListener(listenerRef))

    val messageToNotificationRequestFlow =
      Flow[Message]
      .map {
        case tm: TextMessage.Strict => Some(tm.getStrictText)
        case _ => None
      }
      .filter(_.isDefined)
      .map(_.get.parseJson.convertTo[NotificationRequest])

    val eventToMessageFlow = Flow[ClusterEvent]
      .map(_.toJson.prettyPrint)
      .map(TextMessage.Strict)

    Flow.fromSinkAndSource(messageToNotificationRequestFlow.to(notificationActorSink), notificationActorSource.via(eventToMessageFlow))
  }

  def route(actorSystem: ActorSystem, clusterStatusActor: ActorRef): Route =
    pathPrefix("ws") {
      pathPrefix("notifications") {
        println("Receive ws connection")
        handleWebSocketMessages(notificationsFlow(actorSystem, clusterStatusActor))
      }
    }

}
