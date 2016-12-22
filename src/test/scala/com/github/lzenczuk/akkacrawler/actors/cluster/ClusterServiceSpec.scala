package com.github.lzenczuk.akkacrawler.actors.cluster

import akka.actor.{ActorSystem, Address}
import akka.cluster.Cluster
import akka.testkit.TestKit
import com.github.lzenczuk.akkacrawler.service.cluster.ClusterServiceImpl
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike}

/**
  * Created by dev on 15/12/16.
  */
class ClusterServiceSpec extends TestKit(ActorSystem("cma-spec-as")) with FlatSpecLike with MockitoSugar with BeforeAndAfterAll {


  override protected def afterAll(): Unit = {
    system.terminate()
  }

  "ClusterManagerActor" should "create cluster" in {
    val clusterMock: Cluster = mock[Cluster]
    when(clusterMock.selfAddress).thenReturn(Address("akka.tcp", "test-system", "localhost", 12346))

    val clusterServiceImpl: ClusterServiceImpl = new ClusterServiceImpl(clusterMock)
    clusterServiceImpl.createCluster()

    verify(clusterMock).join(Address("akka.tcp", "test-system", "localhost", 12346))
  }

  "ClusterManagerActor" should "join cluster" in {
    val clusterMock: Cluster = mock[Cluster]

    val clusterServiceImpl: ClusterServiceImpl = new ClusterServiceImpl(clusterMock)
    clusterServiceImpl.joinCluster("test-system", "somehost", 12345)

    verify(clusterMock).join(Address("akka.tcp", "test-system", "somehost", 12345))
  }

  "ClusterManagerActor" should "leave cluster" in {
    val clusterMock: Cluster = mock[Cluster]
    when(clusterMock.selfAddress).thenReturn(Address("akka.tcp", "test-system", "localhost", 12347))

    val clusterServiceImpl: ClusterServiceImpl = new ClusterServiceImpl(clusterMock)
    clusterServiceImpl.leaveCluster()

    verify(clusterMock).leave(Address("akka.tcp", "test-system", "localhost", 12347))
  }

}
