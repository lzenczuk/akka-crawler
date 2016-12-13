package com.github.lzenczuk.akkacrawler

import com.github.lzenczuk.akkacrawler.config.ApplicationModule
import com.github.lzenczuk.akkacrawler.web.WebServer
import com.google.inject.Guice
import net.codingwell.scalaguice.InjectorExtensions._

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by dev on 08/12/16.
  */
object Application extends App{

  val injector = Guice.createInjector(ApplicationModule)

  private val webServer: WebServer = injector.instance[WebServer]

  private val port: Int = Option(System.getProperty("http.port")).map(_.toInt).getOrElse(9898)

  webServer.run("localhost", port).onComplete{
    case Success(_) =>
      println("Server running")
    case Failure(ex) =>
      println(s"Exception when running server: ${ex.getMessage}")
  }
}
