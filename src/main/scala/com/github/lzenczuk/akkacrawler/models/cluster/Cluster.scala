package com.github.lzenczuk.akkacrawler.models.cluster

/**
  * Created by dev on 12/12/16.
  */
case class Address(systemName:String, host:String, port:Int)

trait NodeStatus
case object Joining extends NodeStatus
case object WeaklyUp extends NodeStatus
case object Up extends NodeStatus
case object Leaving extends NodeStatus
case object Exiting extends NodeStatus
case object Removed extends NodeStatus

case class Node(address: Address, status: NodeStatus, reachable:Boolean)

case class Cluster(master:Option[Address], nodes:List[Node])

trait ClusterEvent
case class ClusterInitEvent(cluster: Cluster) extends ClusterEvent
case class NodeUpdateEvent(address: Address, status: NodeStatus, reachable:Boolean) extends ClusterEvent
case class MasterUpdateEvent(address: Option[Address]) extends ClusterEvent
