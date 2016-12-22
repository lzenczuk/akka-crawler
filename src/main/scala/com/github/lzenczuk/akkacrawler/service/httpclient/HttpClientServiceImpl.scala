package com.github.lzenczuk.akkacrawler.service.httpclient

import com.github.lzenczuk.akkacrawler.actors.httpclient.HttpClientActorFactory
import com.github.lzenczuk.akkacrawler.util.IdGenerator

/**
  * Created by dev on 22/12/16.
  */
class HttpClientServiceImpl(httpClientActorFactory: HttpClientActorFactory, idGenerator: IdGenerator) extends HttpClientService{

  override def createClient(): String = {
    val id: String = idGenerator.generate

    httpClientActorFactory.create(id)

    id
  }
}
