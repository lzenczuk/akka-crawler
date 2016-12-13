package com.github.lzenczuk.akkacrawler.models.cluster

/**
  * Created by dev on 13/12/16.
  */
object ClusterModels {

  object ClusterManager {

    trait ClusterManagerCommand
    case object CreateCluster extends ClusterManagerCommand
    case class JoinCluster(system: String, host: String, port: Int) extends ClusterManagerCommand
    case object LeaveCluster extends ClusterManagerCommand

  }

  object ClusterStatus {

    case class Address(systemName: String, host: String, port: Int)

    trait NodeStatus
    case object Joining extends NodeStatus
    case object WeaklyUp extends NodeStatus
    case object Up extends NodeStatus
    case object Leaving extends NodeStatus
    case object Exiting extends NodeStatus
    case object Removed extends NodeStatus

    case class Node(address: Address, status: NodeStatus, reachable: Boolean)

    case class Cluster(master: Option[Address], nodes: List[Node])

    trait ClusterStatusEvent
    case class ClusterInitEvent(cluster: Cluster) extends ClusterStatusEvent
    case class NodeUpdateEvent(address: Address, status: NodeStatus, reachable: Boolean) extends ClusterStatusEvent
    case class MasterUpdateEvent(address: Option[Address]) extends ClusterStatusEvent

  }

}
