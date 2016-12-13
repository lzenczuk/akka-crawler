package com.github.lzenczuk.akkacrawler.web

import javax.inject.{Inject, Singleton}

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.concurrent.Future

/**
  * Created by dev on 02/12/16.
  */

@Singleton
class WebServer @Inject() (implicit actorSystem: ActorSystem, route: Route) {

  implicit val materializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher

  var handleFuture: Option[Future[ServerBinding]] = None

  def run(host:String, port:Int): Future[Unit] = {

    if(handleFuture.isDefined){
      Future[Unit]{
        throw new RuntimeException("Server is already running.")
      }
    }else{
      val hf = Http().bindAndHandle(route, host, port)
      handleFuture = Option(hf)
      hf.map(_ => Done)
    }
  }

  def stop(): Future[Unit] ={

    if (handleFuture.isDefined) {
      handleFuture.get.flatMap(sb => sb.unbind()).map(_ => {
        handleFuture = None
        Done
      })
    } else {
      Future[Unit] {
        throw new RuntimeException("Server not running. Can stop it.")
      }
    }
  }
}
