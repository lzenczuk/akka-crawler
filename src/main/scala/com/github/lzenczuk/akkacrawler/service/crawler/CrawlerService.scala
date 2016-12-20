package com.github.lzenczuk.akkacrawler.service.crawler

import akka.actor.ActorRef
import com.github.lzenczuk.akkacrawler.actors.crawler.CrawlerActorFactory
import com.github.lzenczuk.akkacrawler.models.crawler.CrawlerRequest
import com.github.lzenczuk.akkacrawler.util.IdGenerator

/**
  * Created by dev on 20/12/16.
  */
trait CrawlerService {
  def create(request: CrawlerRequest): String
}

class CrawlerServiceImpl(crawlerActorFactory: CrawlerActorFactory, idGenerator: IdGenerator) extends CrawlerService {
  override def create(request: CrawlerRequest): String = {
    val requestId: String = idGenerator.generate

    val crawlerActor: ActorRef = crawlerActorFactory.create(requestId)
    crawlerActor ! request

    requestId
  }
}