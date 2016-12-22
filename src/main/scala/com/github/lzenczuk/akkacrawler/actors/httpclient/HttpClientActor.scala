package com.github.lzenczuk.akkacrawler.actors.httpclient

import akka.NotUsed
import akka.actor.{ActorRef, FSM, Props, Status}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.github.lzenczuk.akkacrawler.actors.httpclient.HttpClientActor.fsm.{Data, State}
import com.github.lzenczuk.akkacrawler.actors.httpclient.HttpClientActor.{BusyWaitingForResponse, HttpActorException}
import com.github.lzenczuk.akkacrawler.models.httpclient.{CHttpErrorResponse, CHttpRequest, CHttpRequestResponse, CHttpResponse}

import scala.util.{Failure, Success, Try}

/**
  * Created by dev on 15/12/16.
  */

object HttpClientActor {
  def props(id:String) = Props(new HttpClientActor(id))

  object fsm {
    sealed trait State
    case object WaitingForRequest extends State
    case object WaitingForResponse extends State

    sealed trait Data
    case object Empty extends Data
    case class Processing(actorRef: ActorRef, req:CHttpRequest) extends Data
  }

  case class HttpActorException(message:String) extends RuntimeException(message)
  case class BusyWaitingForResponse(request: CHttpRequest)

}

class HttpClientActor(id:String) extends  FSM[State, Data] {
  import HttpClientActor.fsm._

  case object HttpClientFlowComplete

  private implicit val materializer: ActorMaterializer = ActorMaterializer()

  var httpClientFlowActor = createHttpClientFlow()

  override def postStop(): Unit = {
    stateData match {
      case Processing(recipient, req) => recipient ! CHttpRequestResponse(req, CHttpErrorResponse("Http actor stopped."))
      case Empty =>
    }
  }

  startWith(WaitingForRequest, Empty)

  when(WaitingForRequest){
    case Event(request:CHttpRequest, Empty) =>
      cReqToReq(request) match {
        case Left(req) =>
          httpClientFlowActor ! req
          goto(WaitingForResponse) using Processing(sender, request)
        case Right(response) =>
          sender ! CHttpRequestResponse(request, response)
          stay
      }
  }

  when(WaitingForResponse){
    case Event(Success(response:HttpResponse), Processing(recipient, request)) =>
      recipient ! CHttpRequestResponse(request, respToCResp(response))
      goto(WaitingForRequest) using Empty

    case Event(Failure(ex), Processing(recipient, request)) =>
      recipient ! CHttpRequestResponse(request, CHttpErrorResponse(ex.getMessage))
      goto(WaitingForRequest) using Empty

    case Event(Status.Failure(ex), Processing(recipient, request)) =>
      recipient ! CHttpRequestResponse(request, CHttpErrorResponse(ex.getMessage))
      resetHttpClientFlow()
      goto(WaitingForRequest) using Empty

    case Event(request:CHttpRequest, r:Processing) =>
      sender ! BusyWaitingForResponse(request)
      stay()
  }

  initialize()

  log.info(s"HttpClientActor with id $id created.")

  private def createHttpClientFlow():ActorRef = {

    type HttpClientFlowRequest = (HttpRequest, NotUsed)
    type HttpClientFlowResponse = (Try[HttpResponse], NotUsed)

    val clientFlow: Flow[HttpClientFlowRequest, HttpClientFlowResponse, NotUsed] = Http(context.system).superPool[NotUsed]()

    val convertedFlow:Flow[HttpRequest, Try[HttpResponse], NotUsed] = Flow[HttpRequest].map((_, NotUsed)).via(clientFlow).map(_._1)

    Source.actorRef[HttpRequest](1, OverflowStrategy.fail)
      .via(convertedFlow)
      .to(Sink.actorRef[Try[HttpResponse]](self, HttpClientFlowComplete))
      .run()
  }

  private def resetHttpClientFlow(): Unit ={
    httpClientFlowActor = createHttpClientFlow()
  }

  private def cReqToReq(req: CHttpRequest):Either[HttpRequest, CHttpResponse] = {

    val optMethod: Option[HttpMethod] = HttpMethods.getForKey(req.method)
    if (optMethod.isEmpty) {
      return Right(CHttpErrorResponse(s"Incorrect method: ${req.method}"))
    }

    val uri: Uri = Uri(req.url)
    try {
      HttpRequest.verifyUri(uri)
    } catch {
      case iae: IllegalArgumentException => return Right(CHttpErrorResponse(iae.getMessage))
    }

    Left(HttpRequest(optMethod.get, uri))
  }

  private def respToCResp(httpResponse: HttpResponse):CHttpResponse = {
    CHttpResponse(httpResponse.status.intValue(), httpResponse.status.reason())
  }
}
