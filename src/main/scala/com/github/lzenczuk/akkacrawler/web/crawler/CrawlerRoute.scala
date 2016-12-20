package com.github.lzenczuk.akkacrawler.web.crawler

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.github.lzenczuk.akkacrawler.models.crawler.CrawlerRequest
import com.github.lzenczuk.akkacrawler.service.crawler.CrawlerService
import spray.json.DefaultJsonProtocol

/**
  * Created by dev on 20/12/16.
  */
object CrawlerRoute extends SprayJsonSupport with DefaultJsonProtocol {

  case class CreateCrawlerRequestRequest(crawlerRequest: CrawlerRequest)
  case class CreateCrawlerRequestResponse(requestId:String)

  implicit val crawlerRequestFormat = jsonFormat2(CrawlerRequest)
  implicit val createCrawlerRequestRequestFormat = jsonFormat1(CreateCrawlerRequestRequest)
  implicit val createCrawlerRequestResponseFormat = jsonFormat1(CreateCrawlerRequestResponse)

  def route(crawlerService: CrawlerService): Route =
    pathPrefix("crawler"){
      pathEnd {
        post {
          entity(as[CreateCrawlerRequestRequest]){ request:CreateCrawlerRequestRequest =>
            complete(CreateCrawlerRequestResponse(crawlerService.create(request.crawlerRequest)))
          }
        }
      }
    }
}
