package com.github.lzenczuk.akkacrawler.util

import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

/**
  * Created by dev on 20/12/16.
  */
trait IdGenerator {
  def generate:String
}

class JavaUUIDBasedIdGenerator extends IdGenerator {
  override def generate: String = UUID.randomUUID().toString
}

class NanoTimeIncrementCounterIdGenerator extends IdGenerator {

  private val counter: AtomicLong = new AtomicLong(System.nanoTime())

  override def generate: String = counter.getAndIncrement().toString
}
