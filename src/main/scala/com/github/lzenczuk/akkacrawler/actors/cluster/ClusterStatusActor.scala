package com.github.lzenczuk.akkacrawler.actors.cluster

import javax.inject.Inject

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.cluster.ClusterEvent.{MemberExited, MemberLeft, MemberRemoved, _}
import akka.routing.{BroadcastRoutingLogic, Router}
import com.github.lzenczuk.akkacrawler.actors.cluster.ClusterStatusActor.{Subscribe, UnSubscribe}
import com.github.lzenczuk.akkacrawler.models.cluster.ClusterModels.ClusterStatus._

/**
  * Created by dev on 09/12/16.
  */

object ClusterStatusActor {

  def props(cluster:akka.cluster.Cluster):Props = Props(new ClusterStatusActor(cluster))

  trait ClusterStatusQuery
  case class Subscribe(subscriberRef:ActorRef) extends ClusterStatusQuery
  case object UnSubscribe extends ClusterStatusQuery
}

class ClusterStatusActor @Inject() (cluster:akka.cluster.Cluster) extends Actor with ActorLogging{

  val clusterStatus = new ClusterState
  var broadcast = Router(BroadcastRoutingLogic())

  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberEvent], classOf[LeaderChanged])
  }

  def receive = {
    case me:MemberEvent => clusterStatus.process(me).foreach(ce => broadcast.route(ce, self))
    case lc:LeaderChanged => clusterStatus.process(lc).foreach(ce => broadcast.route(ce, self))
    case Subscribe(subscriberRef) =>
      subscriberRef ! clusterStatus.getInitEvent
      context watch subscriberRef
      broadcast = broadcast.addRoutee(subscriberRef)
    case UnSubscribe =>
      broadcast = broadcast.removeRoutee(sender)
      context unwatch sender
    case Terminated(a) =>
      broadcast = broadcast.removeRoutee(a)
  }
}

class ClusterState private[cluster] {

  var master:Option[Address] = None
  var nodes:Map[Address, Node] = Map()

  implicit def akkaAddressToDomainAddress(address:akka.actor.Address):Address = Address(address.system, address.host.getOrElse("unknown"), address.port.getOrElse(-1))

  def process(event:MemberEvent):List[ClusterStatusEvent] = {
    event match {
      case me:MemberJoined => updateNodeStatus(me.member.address, Joining)
      case me:MemberWeaklyUp => updateNodeStatus(me.member.address, WeaklyUp)
      case me:MemberUp => updateNodeStatus(me.member.address, Up)
      case me:MemberLeft => updateNodeStatus(me.member.address, Leaving)
      case me:MemberExited => updateNodeStatus(me.member.address, Exiting)
      case me:MemberRemoved => updateNodeStatus(me.member.address, Removed)
      case _ => List()
    }
  }

  def process(event:LeaderChanged):List[ClusterStatusEvent] = {
    master = event.leader.map(akkaAddressToDomainAddress(_))
    List(MasterUpdateEvent(master))
  }

  def getInitEvent = ClusterInitEvent(Cluster(master, nodes.toList.map(ant => ant._2)))

  private def updateNodeStatus(address: Address, status: NodeStatus):List[ClusterStatusEvent] = {
    nodes = nodes + (address -> Node(address, status, reachable = true))
    List(NodeUpdateEvent(address, status, reachable = true))
  }
}