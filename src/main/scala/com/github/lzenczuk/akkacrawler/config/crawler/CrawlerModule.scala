package com.github.lzenczuk.akkacrawler.config.crawler

import javax.inject.Singleton

import akka.actor.ActorSystem
import com.github.lzenczuk.akkacrawler.actors.crawler.{CrawlerActorFactory, CrawlerActorFactoryImpl}
import com.github.lzenczuk.akkacrawler.service.crawler.{CrawlerService, CrawlerServiceImpl}
import com.github.lzenczuk.akkacrawler.util.IdGenerator
import com.google.inject.{AbstractModule, Provides}

/**
  * Created by dev on 20/12/16.
  */
object CrawlerModule extends AbstractModule{

  override def configure(): Unit = {}

  @Provides @Singleton
  def providesCrawlerActorFactory(actorSystem: ActorSystem):CrawlerActorFactory = new CrawlerActorFactoryImpl(actorSystem)

  @Provides @Singleton
  def providesCrawlerService(crawlerActorFactory: CrawlerActorFactory, idGenerator: IdGenerator):CrawlerService = new CrawlerServiceImpl(crawlerActorFactory, idGenerator)
}
