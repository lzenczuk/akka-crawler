package com.github.lzenczuk.akkacrawler.actors.httpclient

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import com.github.lzenczuk.akkacrawler.actors.httpclient.HttpClientActor.BusyWaitingForResponse
import com.github.lzenczuk.akkacrawler.models.httpclient.{CHttpErrorResponse, CHttpRequest, CHttpRequestResponse, CHttpSuccessResponse}
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
    clientProbe.expectMsg(CHttpRequestResponse(CHttpRequest("GET", "http://localhost:8080/result"), CHttpSuccessResponse(200,"OK")))
  }

  "HttpClientActor" should "fetch not existing page" in {
    val httpClient: TestActorRef[HttpClientActor] = TestActorRef[HttpClientActor]

    val clientProbe = TestProbe()

    clientProbe.send(httpClient, CHttpRequest("GET", "http://localhost:8080/notFound"))
    clientProbe.expectMsg(CHttpRequestResponse(CHttpRequest("GET", "http://localhost:8080/notFound"),CHttpSuccessResponse(404, "Not Found")))
  }

  "HttpClientActor" should "fetch redirected page" in {
    val httpClient: TestActorRef[HttpClientActor] = TestActorRef[HttpClientActor]

    val clientProbe = TestProbe()

    clientProbe.send(httpClient, CHttpRequest("GET", "http://localhost:8080/redirect"))
    clientProbe.expectMsg(CHttpRequestResponse(CHttpRequest("GET", "http://localhost:8080/redirect"), CHttpSuccessResponse(301, "Moved Permanently")))
  }

  "HttpClientActor" should "reject request when waiting for response" in {
    val httpClient: TestActorRef[HttpClientActor] = TestActorRef[HttpClientActor]

    val clientProbe = TestProbe()

    clientProbe.send(httpClient, CHttpRequest("GET", "http://localhost:8080/delay1s"))
    clientProbe.send(httpClient, CHttpRequest("GET", "http://localhost:8080/result"))
    clientProbe.expectMsg(BusyWaitingForResponse(CHttpRequest("GET","http://localhost:8080/result")))
    clientProbe.expectMsg(CHttpRequestResponse(CHttpRequest("GET", "http://localhost:8080/delay1s"), CHttpSuccessResponse(200, "OK")))
  }

  "HttpClientActor" should "return CHttpResponse when method is incorrect" in {
    val httpClient: TestActorRef[HttpClientActor] = TestActorRef[HttpClientActor]

    val clientProbe = TestProbe()

    clientProbe.send(httpClient, CHttpRequest("GETS", "http://localhost:8080/delay1s"))
    clientProbe.expectMsgPF(){
      case CHttpRequestResponse(CHttpRequest("GETS", "http://localhost:8080/delay1s"), ce:CHttpErrorResponse) => assert(true)
      case r => assert(false, s"Expected CHttpRequestResponse with CHttpErrorResponse but was $r")
    }
  }

  "HttpClientActor" should "return CHttpResponse when url is incorrect" in {
    val httpClient: TestActorRef[HttpClientActor] = TestActorRef[HttpClientActor]

    val clientProbe = TestProbe()

    clientProbe.send(httpClient, CHttpRequest("GET", "//localhost:8080/delay1s"))
    clientProbe.expectMsgPF(){
      case CHttpRequestResponse(CHttpRequest("GET", "//localhost:8080/delay1s"), ce:CHttpErrorResponse) => assert(true)
      case r => assert(false, s"Expected CHttpRequestResponse with CHttpErrorResponse but was $r")
    }
  }
}
