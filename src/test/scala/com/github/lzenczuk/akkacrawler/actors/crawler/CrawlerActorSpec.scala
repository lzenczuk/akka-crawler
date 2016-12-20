package com.github.lzenczuk.akkacrawler.actors.crawler

import java.util.UUID

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{ImplicitSender, TestKit}
import com.github.lzenczuk.akkacrawler.actors.crawler.CrawlerActor.Status
import com.github.lzenczuk.akkacrawler.models.httpclient.{CHttpRequest, CHttpSuccessResponse}
import com.github.lzenczuk.akkacrawler.models.httpcrawler.{CrawlerRequest, CrawlerResponse, CrawlerStep}
import com.typesafe.config.ConfigFactory
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.duration._

/**
  * Created by dev on 16/12/16.
  */
class CrawlerActorSpec extends TestKit(ActorSystem("cas-test-as", ConfigFactory.load("inmemory-persistence-application.conf"))) with FlatSpecLike with Matchers with MockitoSugar with ImplicitSender with BeforeAndAfterAll{

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "CrawlerActorSpec" should "process commands" in {

    val randomId: String = UUID.randomUUID().toString

    val crawlerActor = system.actorOf(CrawlerActor.props(randomId))

    crawlerActor ! Status
    expectMsg(15 seconds, None)

    crawlerActor ! CrawlerRequest("GET", "http://localhost/test")

    crawlerActor ! Status
    expectMsgPF(15 seconds){
      case Some(resp:CrawlerResponse) =>
        assert(resp.crawlerRequest==CrawlerRequest("GET", "http://localhost/test"))
        assert(resp.steps.isEmpty)
        println(s"Actor status: $resp")
      case x =>
        assert(false, s"Expecting  Some(CrawlerResponse), was $x")
    }

    crawlerActor ! CrawlerStep(CHttpRequest("GET", "http://localhost/test"), Some(CHttpSuccessResponse(200, "OK")))

    crawlerActor ! Status
    expectMsgPF(15 seconds){
      case Some(resp:CrawlerResponse) =>
        assert(resp.crawlerRequest==CrawlerRequest("GET", "http://localhost/test"))
        assert(resp.steps.size==1)
        assert(resp.steps.head==CrawlerStep(CHttpRequest("GET", "http://localhost/test"), Some(CHttpSuccessResponse(200, "OK"))))
        println(s"Actor status: $resp")
      case x =>
        assert(false, s"Expecting  Some(CrawlerResponse), was $x")
    }

    crawlerActor ! CrawlerStep(CHttpRequest("GET", "http://localhost/test2"), Some(CHttpSuccessResponse(200, "OK")))

    crawlerActor ! Status
    expectMsgPF(15 seconds){
      case Some(resp:CrawlerResponse) =>
        assert(resp.crawlerRequest==CrawlerRequest("GET", "http://localhost/test"))
        assert(resp.steps.size==2)
        assert(resp.steps.head==CrawlerStep(CHttpRequest("GET", "http://localhost/test"), Some(CHttpSuccessResponse(200, "OK"))))
        assert(resp.steps.tail.head==CrawlerStep(CHttpRequest("GET", "http://localhost/test2"), Some(CHttpSuccessResponse(200, "OK"))))
        println(s"Actor status: $resp")
      case x =>
        assert(false, s"Expecting  Some(CrawlerResponse), was $x")
    }

    crawlerActor ! PoisonPill

    val crawlerActorReloaded = system.actorOf(CrawlerActor.props(randomId))

    crawlerActorReloaded ! Status
    expectMsgPF(15 seconds){
      case Some(resp:CrawlerResponse) =>
        assert(resp.crawlerRequest==CrawlerRequest("GET", "http://localhost/test"))
        assert(resp.steps.size==2)
        assert(resp.steps.head==CrawlerStep(CHttpRequest("GET", "http://localhost/test"), Some(CHttpSuccessResponse(200, "OK"))))
        assert(resp.steps.tail.head==CrawlerStep(CHttpRequest("GET", "http://localhost/test2"), Some(CHttpSuccessResponse(200, "OK"))))
        println(s"Actor status: $resp")
      case x =>
        assert(false, s"Expecting  Some(CrawlerResponse), was $x")
    }
  }

}
