package com.github.lzenczuk.akkacrawler.actors.cluster

import akka.actor.{ActorSystem, Address}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{InitialStateAsEvents, LeaderChanged, MemberEvent}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import com.github.lzenczuk.akkacrawler.models.cluster.ClusterModels.ClusterManager.{CreateCluster, JoinCluster, LeaveCluster}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike}

/**
  * Created by dev on 15/12/16.
  */
class ClusterStatusActorSpec extends TestKit(ActorSystem("cma-spec-as")) with FlatSpecLike with MockitoSugar with BeforeAndAfterAll {


  override protected def afterAll(): Unit = {
    system.terminate()
  }

  "ClusterStatusActor" should "subscribe to cluster events" in {
    val clusterMock: Cluster = mock[Cluster]
    when(clusterMock.selfAddress).thenReturn(Address("akka.tcp", "test-system", "localhost", 12346))

    val clusterStatusActor = TestActorRef(ClusterStatusActor.props(clusterMock))

    verify(clusterMock).subscribe(clusterStatusActor, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[LeaderChanged])
  }

  /*"ClusterStatusActor" should "subscribe to cluster events" in {
    val clusterMock: Cluster = mock[Cluster]
    when(clusterMock.selfAddress).thenReturn(Address("akka.tcp", "test-system", "localhost", 12346))

    val externalActor = TestProbe()

    val clusterStatusActor = TestActorRef(ClusterStatusActor.props(clusterMock))

    externalActor.send(clusterStatusActor, CreateCluster)
    externalActor.expectNoMsg()

    verify(clusterMock).join(Address("akka.tcp", "test-system", "localhost", 12346))
  }*/
}
