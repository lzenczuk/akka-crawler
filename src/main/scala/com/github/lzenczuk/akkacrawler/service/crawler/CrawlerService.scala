package com.github.lzenczuk.akkacrawler.service.crawler

import com.github.lzenczuk.akkacrawler.models.crawler.CrawlerRequest

/**
  * Created by dev on 20/12/16.
  */
trait CrawlerService {
  def create(request: CrawlerRequest): String
}

