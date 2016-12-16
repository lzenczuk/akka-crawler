package com.github.lzenczuk.akkacrawler.actors.httpclient

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import com.github.lzenczuk.akkacrawler.actors.httpclient.HttpClientActor.BusyWaitingForResponse
import com.github.lzenczuk.akkacrawler.models.httpclient.HttpClientModels.{CHttpRequest, CHttpResponse, CHttpSuccessResponse}
import com.github.tomakehurst.wiremock.WireMockServer
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike}
import org.scalatest.mockito.MockitoSugar
import com.github.tomakehurst.wiremock.client.WireMock._

/**
  * Created by dev on 15/12/16.
  */
class HttpClientActorSpec extends TestKit(ActorSystem("hca-spec-as")) with FlatSpecLike with MockitoSugar with BeforeAndAfterAll {

  var wireMockServer: WireMockServer = null

  override protected def beforeAll(): Unit = {
    wireMockServer = new WireMockServer()
    wireMockServer.start()

    stubFor(get(urlEqualTo("/result")).willReturn(
      aResponse()
        .withStatus(200)
        .withStatusMessage("OK")
        .withHeader("Content-Type", "text/plain")
        .withBody("result")
    )
    )

    stubFor(get(urlEqualTo("/notFound")).willReturn(
      aResponse()
        .withStatus(404)
        .withStatusMessage("Not Found")
        .withHeader("Content-Type", "text/plain")
        .withBody("not found")
    )
    )

    stubFor(get(urlEqualTo("/redirect")).willReturn(
      aResponse()
        .withStatus(301)
        .withStatusMessage("Moved Permanently")
        .withHeader("Location", "/result")
        .withHeader("Content-Type", "text/plain")
        .withBody("redirect")
    )
    )

    stubFor(get(urlEqualTo("/delay1s")).willReturn(
      aResponse()
        .withStatus(200)
        .withStatusMessage("OK")
        .withHeader("Location", "/result")
        .withHeader("Content-Type", "text/plain")
        .withBody("redirect")
        .withFixedDelay(1000)
    )
    )
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
    system.terminate()
  }

  "HttpClientActor" should "fetch existing page" in {
    val httpClient: TestActorRef[HttpClientActor] = TestActorRef[HttpClientActor]

    val clientProbe = TestProbe()

    clientProbe.send(httpClient, CHttpRequest("GET", "http://localhost:8080/result"))
    clientProbe.expectMsg(CHttpSuccessResponse(200, "OK"))
  }

  "HttpClientActor" should "fetch not existing page" in {
    val httpClient: TestActorRef[HttpClientActor] = TestActorRef[HttpClientActor]

    val clientProbe = TestProbe()

    clientProbe.send(httpClient, CHttpRequest("GET", "http://localhost:8080/notFound"))
    clientProbe.expectMsg(CHttpSuccessResponse(404, "Not Found"))
  }

  "HttpClientActor" should "fetch redirected page" in {
    val httpClient: TestActorRef[HttpClientActor] = TestActorRef[HttpClientActor]

    val clientProbe = TestProbe()

    clientProbe.send(httpClient, CHttpRequest("GET", "http://localhost:8080/redirect"))
    clientProbe.expectMsg(CHttpSuccessResponse(301, "Moved Permanently"))
  }

  "HttpClientActor" should "reject request when waiting for response" in {
    val httpClient: TestActorRef[HttpClientActor] = TestActorRef[HttpClientActor]

    val clientProbe = TestProbe()

    clientProbe.send(httpClient, CHttpRequest("GET", "http://localhost:8080/delay1s"))
    clientProbe.send(httpClient, CHttpRequest("GET", "http://localhost:8080/result"))
    clientProbe.expectMsg(BusyWaitingForResponse(CHttpRequest("GET","http://localhost:8080/result")))
    clientProbe.expectMsg(CHttpSuccessResponse(200, "OK"))
  }
}
