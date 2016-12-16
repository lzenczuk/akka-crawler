package com.github.lzenczuk.akkacrawler.actors.httpclient

import akka.NotUsed
import akka.actor.{ActorRef, FSM, Props, Status}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.github.lzenczuk.akkacrawler.actors.httpclient.HttpClientActor.fsm.{Data, State}
import com.github.lzenczuk.akkacrawler.actors.httpclient.HttpClientActor.{BusyWaitingForResponse, HttpActorException}
import com.github.lzenczuk.akkacrawler.models.httpclient.HttpClientModels.{CHttpErrorResponse, CHttpRequest, CHttpResponse}

import scala.util.{Failure, Success, Try}

/**
  * Created by dev on 15/12/16.
  */

object HttpClientActor {
  def props = Props(new HttpClientActor)

  object fsm {
    sealed trait State
    case object WaitingForRequest extends State
    case object WaitingForResponse extends State

    sealed trait Data
    case object Empty extends Data
    case class Recipient(actorRef: ActorRef) extends Data
  }

  case class HttpActorException(message:String) extends RuntimeException(message)
  case class BusyWaitingForResponse(request: CHttpRequest)

}

class HttpClientActor extends  FSM[State, Data] {
  import HttpClientActor.fsm._

  case object HttpClientFlowComplete

  private implicit val materializer: ActorMaterializer = ActorMaterializer()

  var httpClientFlowActor = createHttpClientFlow()

  override def postStop(): Unit = {
    stateData match {
      case Recipient(recipient) =>
        recipient ! Failure(HttpActorException(s"Http actor stopped."))
    }
  }

  startWith(WaitingForRequest, Empty)

  when(WaitingForRequest){
    case Event(request:CHttpRequest, Empty) =>
      cReqToReq(request) match {
        case Left(request) =>
          httpClientFlowActor ! request
          goto(WaitingForResponse) using Recipient(sender)
        case Right(response) =>
          sender ! response
          stay
      }
  }

  when(WaitingForResponse){
    case Event(Success(response:HttpResponse), Recipient(recipient)) =>
      recipient ! respToCResp(response)
      goto(WaitingForRequest) using Empty

    case Event(Failure(ex), Recipient(recipient)) =>
      recipient ! CHttpErrorResponse(ex.getMessage)
      goto(WaitingForRequest) using Empty

    case Event(Status.Failure(ex), Recipient(recipient)) =>
      recipient ! CHttpErrorResponse(ex.getMessage)
      resetHttpClientFlow()
      goto(WaitingForRequest) using Empty

    case Event(request:CHttpRequest, r:Recipient) =>
      sender ! BusyWaitingForResponse(request)
      stay()
  }

  initialize()

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
