package com.github.lzenczuk.akkacrawler.config.util

import javax.inject.Singleton

import com.github.lzenczuk.akkacrawler.util.{IdGenerator, JavaUUIDBasedIdGenerator}
import com.google.inject.{AbstractModule, Provides}

/**
  * Created by dev on 20/12/16.
  */
object UtilModule extends AbstractModule{
  override def configure(): Unit = {}

  @Provides @Singleton
  def providesIdGenerator():IdGenerator = new JavaUUIDBasedIdGenerator
}
