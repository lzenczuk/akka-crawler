package com.github.lzenczuk.akkacrawler.util

import java.util.UUID

/**
  * Created by dev on 20/12/16.
  */
trait IdGenerator {
  def generate:String
}

class JavaUUIDBasedIdGenerator extends IdGenerator {
  override def generate: String = UUID.randomUUID().toString
}
