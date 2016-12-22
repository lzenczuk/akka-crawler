package com.github.lzenczuk.akkacrawler.web.httpclient

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.github.lzenczuk.akkacrawler.service.httpclient.HttpClientService
import spray.json.DefaultJsonProtocol

/**
  * Created by dev on 20/12/16.
  */
object HttpClientRoute extends SprayJsonSupport with DefaultJsonProtocol {

  // TODO - allow to create clients on specific machines in cluster or in specific "groups"
  //case object CreateHttpClientRequest
  case class CreateHttpClientResponse(id:String)

  //implicit val crawlerRequestFormat = jsonFormat2(CrawlerRequest)
  //implicit val createCrawlerRequestRequestFormat = jsonFormat1(CreateHttpClientRequest)
  implicit val createCrawlerRequestResponseFormat = jsonFormat1(CreateHttpClientResponse)

  def route(httpClientService: HttpClientService): Route =
    pathPrefix("http-client"){
      pathEnd {
        post {
          //entity(as[CreateHttpClientRequest]){ request:CreateHttpClientRequest =>
            complete(CreateHttpClientResponse(httpClientService.createClient()))
          //}
        }
      }
    }
}
