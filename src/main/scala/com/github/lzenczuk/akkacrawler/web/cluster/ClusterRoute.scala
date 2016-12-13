package com.github.lzenczuk.akkacrawler.web.cluster

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.lzenczuk.akkacrawler.actors.cluster.ClusterManagerActor
import spray.json.DefaultJsonProtocol

/**
  * Created by dev on 08/12/16.
  */
object ClusterRoute extends SprayJsonSupport with DefaultJsonProtocol {

  case class Cluster(system: String, host:String, port:Int)

  case class ClusterJoinRequest(cluster: Cluster)

  case class ClusterJoinResponse(success: Boolean, message: String)

  implicit val clusterFormat = jsonFormat3(Cluster)
  implicit val clusterJoinRequestFormat = jsonFormat1(ClusterJoinRequest)
  implicit val clusterJoinResponseFormat = jsonFormat2(ClusterJoinResponse)

  def route(actorSystem: ActorSystem, nodeManager: ActorRef): Route =
    pathPrefix("cluster") {
      pathPrefix("join") {
        pathEnd {
          post {
            entity(as[ClusterJoinRequest]) { cjr: ClusterJoinRequest =>
              nodeManager ! ClusterManagerActor.JoinCluster(cjr.cluster.system, cjr.cluster.host, cjr.cluster.port)
              complete(ClusterJoinResponse(success = true, s"Joining cluster: $cjr"))
            }
          }
        }
      } ~
        pathPrefix("create") {
          pathEnd {
            post {
              nodeManager ! ClusterManagerActor.CreateCluster
              complete(ClusterJoinResponse(success = true, s"Creating cluster"))
            }
          }
        } ~
        pathPrefix("leave") {
          pathEnd {
            post {
              nodeManager ! ClusterManagerActor.LeaveCluster
              complete(ClusterJoinResponse(success = true, s"Leaving cluster"))
            }
          }
        }
    }
}
