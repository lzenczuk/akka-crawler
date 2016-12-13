package com.github.lzenczuk.akkacrawler.web.notification

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.github.lzenczuk.akkacrawler.models.cluster.ClusterModels.ClusterStatus.{ClusterInitEvent, ClusterStatusEvent, MasterUpdateEvent, NodeUpdateEvent, _}
import com.github.lzenczuk.akkacrawler.models.notification.NotificationModels.{CancelClusterNotificationSubscription, NotificationCommand, SubscribeClusterNotification}
import spray.json._

/**
  * Created by dev on 13/12/16.
  */
trait NotificationJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol{

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

  implicit object ClusterEventFormat extends RootJsonFormat[ClusterStatusEvent] {
    override def write(obj: ClusterStatusEvent): JsValue = {
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

    override def read(value: JsValue): ClusterStatusEvent = {
      value.asJsObject.getFields("type", "body") match {
        case Seq(JsString("ClusterInitEvent"), body: JsObject) => body.convertTo[ClusterInitEvent]
        case Seq(JsString("NodeUpdateEvent"), body: JsObject) => body.convertTo[NodeUpdateEvent]
        case Seq(JsString("MasterUpdateEvent"), body: JsObject) => body.convertTo[MasterUpdateEvent]
      }
    }
  }

  implicit object NotificationRequestFormat extends RootJsonFormat[NotificationCommand] {
    override def write(obj: NotificationCommand): JsValue = {
      obj match {
        case SubscribeClusterNotification => toJsObjectWithType("SubscribeClusterNotification")
        case CancelClusterNotificationSubscription => toJsObjectWithType("CancelClusterNotification")
      }
    }

    def toJsObjectWithType(typeName: String): JsObject = JsObject(
      "type" -> JsString(typeName)
    )

    override def read(value: JsValue): NotificationCommand = {
      value.asJsObject.getFields("type") match {
        case Seq(JsString("SubscribeClusterNotification")) => SubscribeClusterNotification
        case Seq(JsString("CancelClusterNotification")) => CancelClusterNotificationSubscription
      }
    }
  }

}
