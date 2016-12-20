package com.github.lzenczuk.akkacrawler.service.crawler

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{TestKit, TestProbe}
import com.github.lzenczuk.akkacrawler.actors.httpcrawler.{CrawlerActor, CrawlerActorFactory}
import com.github.lzenczuk.akkacrawler.models.httpcrawler.CrawlerRequest
import com.github.lzenczuk.akkacrawler.util.IdGenerator
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FlatSpec, FlatSpecLike, Matchers}

/**
  * Created by dev on 20/12/16.
  */
class CrawlerServiceImplSpec extends TestKit(ActorSystem("csis-as")) with FlatSpecLike with Matchers with MockitoSugar with BeforeAndAfterAll{

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "CrawlerServiceImpl" should "create new crawler actor" in {
    val genReqId: String = "1234567890qwertyuio"

    val crawlerActorProb: TestProbe = TestProbe()
    val crawlerActorFactoryMock: CrawlerActorFactory = mock[CrawlerActorFactory]
    when(crawlerActorFactoryMock.create(genReqId)).thenReturn(crawlerActorProb.ref)

    val idGeneratorMock: IdGenerator = mock[IdGenerator]
    when(idGeneratorMock.generate).thenReturn(genReqId)

    val crawlerServiceImpl: CrawlerServiceImpl = new CrawlerServiceImpl(crawlerActorFactoryMock, idGeneratorMock)

    val requestId:String = crawlerServiceImpl.create(CrawlerRequest("GET", "http://localhost/test"))

    requestId should equal(genReqId)
    crawlerActorProb.expectMsg(CrawlerRequest("GET", "http://localhost/test"))
  }
}
