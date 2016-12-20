package com.github.lzenczuk.akkacrawler.config

import com.github.lzenczuk.akkacrawler.config.akka.AkkaModule
import com.github.lzenczuk.akkacrawler.config.cluster.ClusterModule
import com.github.lzenczuk.akkacrawler.config.crawler.CrawlerModule
import com.github.lzenczuk.akkacrawler.config.util.UtilModule
import com.github.lzenczuk.akkacrawler.config.web.WebModule
import com.google.inject.AbstractModule

/**
  * Created by dev on 09/12/16.
  */
object ApplicationModule extends AbstractModule{
  override def configure(): Unit = {
    install(UtilModule)
    install(AkkaModule)
    install(ClusterModule)
    install(CrawlerModule)
    install(WebModule)
  }
}
