package com.github.lzenczuk.akkacrawler.actors.cluster

import javax.inject.Inject

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.Cluster
import com.github.lzenczuk.akkacrawler.actors.cluster.ClusterManagerActor.{CreateCluster, JoinCluster, LeaveCluster}

/**
  * Created by dev on 08/12/16.
  */

object ClusterManagerActor{

  trait ClusterManagerCommand
  case object CreateCluster extends ClusterManagerCommand
  case class JoinCluster(system: String, host:String, port:Int) extends ClusterManagerCommand
  case object LeaveCluster extends ClusterManagerCommand

  def props(cluster:Cluster):Props = Props(new ClusterManagerActor(cluster))
}

class ClusterManagerActor @Inject()(cluster:Cluster) extends Actor with ActorLogging{

  override def receive = {
    case JoinCluster(system, host, port) => cluster.join(akka.actor.Address("akka.tcp", system, host, port))
    case CreateCluster => cluster.join(cluster.selfAddress)
    case LeaveCluster => cluster.leave(cluster.selfAddress)
  }
}
