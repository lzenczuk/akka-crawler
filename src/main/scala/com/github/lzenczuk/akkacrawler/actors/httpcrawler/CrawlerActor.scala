package com.github.lzenczuk.akkacrawler.actors.httpcrawler

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.github.lzenczuk.akkacrawler.actors.httpcrawler.CrawlerActor._
import com.github.lzenczuk.akkacrawler.models.httpcrawler.{CrawlerRequest, CrawlerResponse, CrawlerStep}

/**
  * Created by dev on 16/12/16.
  */

object CrawlerActor{
  def props(requestId:String, manager:ActorRef) = Props(new CrawlerActor(requestId, manager))

  trait CrawlerActorEvent
  case class Initialized(crawlerRequest: CrawlerRequest) extends CrawlerActorEvent
  case class StepUpdated(crawlerStep: CrawlerStep) extends CrawlerActorEvent

  trait CrawlerActorPersistenceEvent
  case class Ready(requestId:String)
  case object Status
}

class CrawlerActor(requestId:String, manager:ActorRef) extends PersistentActor with ActorLogging{

  var crawlerResponse:Option[CrawlerResponse] = None

  override def persistenceId: String = requestId

  override def receiveCommand: Receive = {
    case cr:CrawlerRequest =>
      log.info(s"Receive command: $cr")
      persist(Initialized(cr))(applyEvent)
      log.info(s"Command processed")
    case cs:CrawlerStep =>
      log.info(s"Receive command: $cs")
      persist(StepUpdated(cs))(applyEvent)
      log.info(s"Command processed")
    case Status =>
      log.info("Receive status request")
      sender ! crawlerResponse
      log.info(s"Status send")
  }

  override def receiveRecover: Receive = {
    case cae:CrawlerActorEvent =>
      log.info(s"Receive recovery message: $cae")
      applyEvent(cae)
    case RecoveryCompleted => manager ! Ready(persistenceId)
    case ev => log.error(s"In receiveRecover receive unknown event: $ev")
  }

  def applyEvent(crawlerActorEvent: CrawlerActorEvent): Unit = {
    log.info(s"Apply event: $crawlerActorEvent")

    crawlerActorEvent match {
      case Initialized(cr) => crawlerResponse = Some(CrawlerResponse(None, cr, List()))
      case StepUpdated(crawlerStep) => crawlerResponse = crawlerResponse.map(cr => cr.copy(steps = cr.steps :+ crawlerStep))
    }

    log.info(s"Event applied")
  }

  log.info(s"CrawlerActor with id $persistenceId created")

}
