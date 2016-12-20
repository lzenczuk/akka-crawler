package com.github.lzenczuk.akkacrawler.actors.crawler

import akka.actor.{Actor, ActorLogging, Props}
import akka.persistence.query.PersistenceQuery
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink

/**
  * Created by dev on 20/12/16.
  */

object CrawlersStatusActor {
  def props = Props(new CrawlersStatusActor)

  case object Status

  case class CrawlerStatus(requestId:String)
}

class CrawlersStatusActor extends Actor with ActorLogging{

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val readJournal: LeveldbReadJournal = PersistenceQuery(context.system).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)

  readJournal.allPersistenceIds().runWith(Sink.actorRef(self, ""))

  def receive = {
    case m => println(s"Receive message: $m")
  }

}
