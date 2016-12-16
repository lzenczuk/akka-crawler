package com.github.lzenczuk.akkacrawler.models.httpcrawler

import com.github.lzenczuk.akkacrawler.models.httpclient.{CHttpRequest, CHttpResponse}

/**
  * Created by dev on 16/12/16.
  */

case class CrawlerStep(req: CHttpRequest, res: Option[CHttpResponse])

case class CrawlerRequest(method: String, url: String)

case class CrawlerResponse(error: Option[String], crawlerRequest: CrawlerRequest, steps: List[CrawlerStep])
