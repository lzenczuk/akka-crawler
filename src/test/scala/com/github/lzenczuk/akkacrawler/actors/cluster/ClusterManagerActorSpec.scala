package com.github.lzenczuk.akkacrawler.actors.cluster

import akka.actor.{ActorSystem, Address}
import akka.cluster.Cluster
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import com.github.lzenczuk.akkacrawler.models.cluster.ClusterModels.ClusterManager.{CreateCluster, JoinCluster, LeaveCluster}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike}

/**
  * Created by dev on 15/12/16.
  */
class ClusterManagerActorSpec extends TestKit(ActorSystem("cma-spec-as")) with FlatSpecLike with MockitoSugar with BeforeAndAfterAll {


  override protected def afterAll(): Unit = {
    system.terminate()
  }

  "ClusterManagerActor" should "create cluster" in {
    val clusterMock: Cluster = mock[Cluster]
    when(clusterMock.selfAddress).thenReturn(Address("akka.tcp", "test-system", "localhost", 12346))

    val externalActor = TestProbe()

    val clusterManagerActor = TestActorRef(ClusterManagerActor.props(clusterMock))

    externalActor.send(clusterManagerActor, CreateCluster)
    externalActor.expectNoMsg()

    verify(clusterMock).join(Address("akka.tcp", "test-system", "localhost", 12346))
  }

  "ClusterManagerActor" should "join cluster" in {
    val clusterMock: Cluster = mock[Cluster]
    val externalActor = TestProbe()

    val clusterManagerActor = TestActorRef(ClusterManagerActor.props(clusterMock))

    externalActor.send(clusterManagerActor, JoinCluster("test-system", "somehost", 12345))
    externalActor.expectNoMsg()

    verify(clusterMock).join(Address("akka.tcp", "test-system", "somehost", 12345))
  }

  "ClusterManagerActor" should "leave cluster" in {
    val clusterMock: Cluster = mock[Cluster]
    when(clusterMock.selfAddress).thenReturn(Address("akka.tcp", "test-system", "localhost", 12347))

    val externalActor = TestProbe()

    val clusterManagerActor = TestActorRef(ClusterManagerActor.props(clusterMock))

    externalActor.send(clusterManagerActor, LeaveCluster)
    externalActor.expectNoMsg()

    verify(clusterMock).leave(Address("akka.tcp", "test-system", "localhost", 12347))
  }

}
