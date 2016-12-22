package com.github.lzenczuk.akkacrawler.config.httpclient

import javax.inject.Singleton

import akka.actor.ActorSystem
import com.github.lzenczuk.akkacrawler.actors.crawler.{CrawlerActorFactory, CrawlerActorFactoryImpl}
import com.github.lzenczuk.akkacrawler.actors.httpclient.{HttpClientActorFactory, HttpClientActorFactoryImpl}
import com.github.lzenczuk.akkacrawler.service.crawler.{CrawlerService, CrawlerServiceImpl}
import com.github.lzenczuk.akkacrawler.service.httpclient.{HttpClientService, HttpClientServiceImpl}
import com.github.lzenczuk.akkacrawler.util.IdGenerator
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}

/**
  * Created by dev on 20/12/16.
  */
object HttpClientModule extends AbstractModule{

  override def configure(): Unit = {}

  @Provides @Singleton
  def providesHttpClientActorFactory(actorSystem: ActorSystem):HttpClientActorFactory = new HttpClientActorFactoryImpl(actorSystem)

  @Provides @Singleton
  def providesHttpClientService(httpClientActorFactory: HttpClientActorFactory, @Named("http-clients-id-generator") idGenerator: IdGenerator):HttpClientService = new HttpClientServiceImpl(httpClientActorFactory, idGenerator)
}
