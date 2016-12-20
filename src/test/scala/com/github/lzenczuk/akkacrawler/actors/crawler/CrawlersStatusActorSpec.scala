package com.github.lzenczuk.akkacrawler.actors.crawler

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import com.github.lzenczuk.akkacrawler.models.crawler.CrawlerRequest
import com.github.lzenczuk.akkacrawler.service.crawler.CrawlerServiceImpl
import com.github.lzenczuk.akkacrawler.util.JavaUUIDBasedIdGenerator
import org.scalatest.FlatSpecLike

/**
  * Created by dev on 20/12/16.
  */
class CrawlersStatusActorSpec extends TestKit(ActorSystem("csas-as")) with FlatSpecLike with ImplicitSender{

  "CrawlersStatusActor" should "provide statuses of all crawler actors" in {

    val crawlerService: CrawlerServiceImpl = new CrawlerServiceImpl(new CrawlerActorFactoryImpl(system), new JavaUUIDBasedIdGenerator)

    val crawlersStatusActor: ActorRef = system.actorOf(CrawlersStatusActor.props)


    val requestsIds = (1 to 100).map(n => {
      crawlerService.create(CrawlerRequest("GET", s"http://localhost/test$n"))
    }).toList

    Thread.sleep(5000)
  }
}
