package com.github.lzenczuk.akkacrawler.actors.httpcrawler

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import akka.util.Timeout
import com.github.lzenczuk.akkacrawler.actors.httpcrawler.CrawlerActor.{Ready, Status}
import com.github.lzenczuk.akkacrawler.models.httpclient.{CHttpRequest, CHttpSuccessResponse}
import com.github.lzenczuk.akkacrawler.models.httpcrawler.{CrawlerRequest, CrawlerResponse, CrawlerStep}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.duration._

/**
  * Created by dev on 16/12/16.
  */
class CrawlerActorSpec extends TestKit(ActorSystem("cas-test-as")) with FlatSpecLike with Matchers with MockitoSugar with ImplicitSender{

  implicit val futureTimeout = Timeout(5 seconds)

  "CrawlerActorSpec" should "process commands" in {

    val randomId: String = UUID.randomUUID().toString

    val managerProbe = TestProbe()
    val crawlerActor: TestActorRef[CrawlerActor] = TestActorRef(CrawlerActor.props(randomId, managerProbe.ref))

    println("Waiting for ready")
    managerProbe.expectMsg(15 seconds, Ready(randomId))
    println("Ready")

    println("Status request")
    crawlerActor ! Status
    expectMsg(15 seconds, None)
    println("Status OK")

    println("Request")
    crawlerActor ! CrawlerRequest("GET", "http://localhost/test")
    println("Request delivered")

    println("Status request")
    crawlerActor ! Status
    expectMsgPF(15 seconds){
      case Some(resp:CrawlerResponse) =>
        assert(resp.crawlerRequest==CrawlerRequest("GET", "http://localhost/test"))
        assert(resp.steps.isEmpty)
      case x =>
        assert(false, s"Expecting  Some(CrawlerResponse), was $x")
    }
    println("Status OK")

    println("Step")
    crawlerActor ! CrawlerStep(CHttpRequest("GET", "http://localhost/test"), Some(CHttpSuccessResponse(200, "OK")))
    println("Step delivered")

    println("Status request")
    crawlerActor ! Status
    expectMsgPF(15 seconds){
      case Some(resp:CrawlerResponse) =>
        assert(resp.crawlerRequest==CrawlerRequest("GET", "http://localhost/test"))
        assert(resp.steps.size==1)
        assert(resp.steps(0)==CrawlerStep(CHttpRequest("GET", "http://localhost/test"), Some(CHttpSuccessResponse(200, "OK"))))
      case x =>
        assert(false, s"Expecting  Some(CrawlerResponse), was $x")
    }
    println("Status OK")

    crawlerActor.stop()
  }

}
