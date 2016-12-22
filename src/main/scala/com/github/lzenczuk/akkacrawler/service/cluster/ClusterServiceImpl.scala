package com.github.lzenczuk.akkacrawler.service.cluster

import akka.cluster.Cluster

/**
  * Created by dev on 22/12/16.
  */
class ClusterServiceImpl(cluster:Cluster) extends ClusterService {

  override def createCluster(): Unit = cluster.join(cluster.selfAddress)

  override def leaveCluster(): Unit = cluster.leave(cluster.selfAddress)

  override def joinCluster(system: String, host: String, port: Int): Unit = cluster.join(akka.actor.Address("akka.tcp", system, host, port))
}
