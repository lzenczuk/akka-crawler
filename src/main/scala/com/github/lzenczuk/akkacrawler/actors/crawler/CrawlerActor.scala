package com.github.lzenczuk.akkacrawler.actors.crawler

import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.github.lzenczuk.akkacrawler.actors.crawler.CrawlerActor._
import com.github.lzenczuk.akkacrawler.models.crawler.{CrawlerRequest, CrawlerResponse, CrawlerStep}

/**
  * Created by dev on 16/12/16.
  */

object CrawlerActor{
  def props(requestId:String) = Props(new CrawlerActor(requestId))

  trait CrawlerActorEvent
  case class Initialized(crawlerRequest: CrawlerRequest) extends CrawlerActorEvent
  case class StepUpdated(crawlerStep: CrawlerStep) extends CrawlerActorEvent

  trait CrawlerActorPersistenceEvent
  case object Status
}

class CrawlerActor(requestId:String) extends PersistentActor with ActorLogging{

  var crawlerResponse:Option[CrawlerResponse] = None

  override def persistenceId: String = requestId

  override def receiveCommand: Receive = {
    case cr:CrawlerRequest => persist(Initialized(cr))(applyEvent)
    case cs:CrawlerStep => persist(StepUpdated(cs))(applyEvent)
    case Status => sender ! crawlerResponse
  }

  override def receiveRecover: Receive = {
    case cae:CrawlerActorEvent => applyEvent(cae)
    case RecoveryCompleted => log.debug(s"CrawlerActor $persistenceId recovered.")
    case ev => log.error(s"In receiveRecover receive unknown event: $ev")
  }

  def applyEvent(crawlerActorEvent: CrawlerActorEvent): Unit = {
    crawlerActorEvent match {
      case Initialized(cr) => crawlerResponse = Some(CrawlerResponse(None, cr, List()))
      case StepUpdated(crawlerStep) => crawlerResponse = crawlerResponse.map(cr => cr.copy(steps = cr.steps :+ crawlerStep))
    }
  }
}
