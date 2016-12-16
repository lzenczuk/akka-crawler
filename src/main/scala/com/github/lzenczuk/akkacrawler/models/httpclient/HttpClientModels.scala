package com.github.lzenczuk.akkacrawler.models.httpclient

import scala.util.Try

/**
  * Created by dev on 15/12/16.
  */
object HttpClientModels {

  case class CHttpRequest(method:String, url:String)

  object CHttpResponse{
    def apply(error: String): CHttpResponse = CHttpErrorResponse(error)
    def apply(status: Int, statusMessage: String): CHttpResponse = CHttpSuccessResponse(status, statusMessage)
  }

  trait CHttpResponse
  case class CHttpErrorResponse(error:String) extends CHttpResponse
  case class CHttpSuccessResponse(status:Int, statusMessage:String) extends CHttpResponse

  case class CHttpStep(req:CHttpRequest, res:Try[CHttpResponse])

  case class CrawlerRequest(method:String, url:String)
  case class CrawlerResponse(error:Option[String], crawlerRequest: CrawlerRequest, steps:List[CHttpStep])

}
