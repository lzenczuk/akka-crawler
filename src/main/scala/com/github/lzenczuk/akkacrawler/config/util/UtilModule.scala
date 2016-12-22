package com.github.lzenczuk.akkacrawler.config.util

import javax.inject.Singleton

import com.github.lzenczuk.akkacrawler.util.{IdGenerator, JavaUUIDBasedIdGenerator, NanoTimeIncrementCounterIdGenerator}
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}

/**
  * Created by dev on 20/12/16.
  */
object UtilModule extends AbstractModule{
  override def configure(): Unit = {}

  @Provides @Singleton @Named("crawlers-id-generator")
  def providesCrawlersIdGenerator():IdGenerator = new JavaUUIDBasedIdGenerator

  @Provides @Singleton @Named("http-clients-id-generator")
  def providesHttpClientsIdGenerator():IdGenerator = new NanoTimeIncrementCounterIdGenerator
}
